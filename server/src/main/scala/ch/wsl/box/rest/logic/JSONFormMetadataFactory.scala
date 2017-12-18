package ch.wsl.box.rest.logic

import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.boxentities.Field.{FieldFile_row, Field_i18n_row, Field_row}
import ch.wsl.box.rest.boxentities.Form.{Form, Form_row}
import ch.wsl.box.rest.boxentities.{Field, Form}
import ch.wsl.box.rest.utils.Auth
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe._
import io.circe.parser._
import io.circe.syntax._

/**
  * Created by andreaminetti on 10/03/16.
  *
  * mapping from form specs in box schema into JSONForm
  */
case class JSONFormMetadataFactory(implicit db:Database) {
  def list: Future[Seq[String]] = Auth.boxDB.run{
    Form.table.result
  }.map{_.map(_.name)}

  import ch.wsl.box.shared.utils.JsonUtils._

  def of(id:Int, lang:String):Future[JSONMetadata] = {
    val formQuery: Query[Form, Form_row, Seq] = for {
      form <- Form.table if form.form_id === id
    } yield form
    getForm(formQuery,lang)
  }

  def of(name:String, lang:String):Future[JSONMetadata] = {
    val formQuery: Query[Form, Form_row, Seq] = for {
      form <- Form.table if form.name === name
    } yield form
    getForm(formQuery,lang)
  }

  def children(form:JSONMetadata):Future[Seq[JSONMetadata]] = {
    val result = Future.sequence{
      form.fields.flatMap(_.child).map{ child =>
        of(child.objId,form.lang)
      }
    }
    val subresults = result.map(x => Future.sequence(x.map(children)))
    for{
      firstLevel <- result
      secondLevel <- subresults
      thirdLevel <- secondLevel.map(_.flatten)
    } yield {
      firstLevel ++ thirdLevel
    }

  }

  private def getForm(formQuery: Query[Form,Form_row,Seq],lang:String) = {

    import io.circe.generic.auto._
    import ch.wsl.box.shared.utils.Formatters._

    def fieldQuery(formId:Int) = for{
      field <- Field.table if field.form_id === formId
      fieldI18n <- Field.Field_i18n if fieldI18n.lang === lang && fieldI18n.field_id === field.field_id
    } yield (field,fieldI18n)

    for{
      form <- Auth.boxDB.run( formQuery.result ).map(_.head)
      fields <- Auth.boxDB.run{fieldQuery(form.form_id.get).result}
      fieldsFile <- Future.sequence(fields.map { case (f, _) =>
        Auth.boxDB.run {
          Field.FieldFile.filter(_.field_id === f.field_id).result
        }.map(_.headOption)
      })
      columns <- Future.sequence(fields.map(f => columns(form,f._1)))
      keys <- JSONMetadataFactory.keysOf(form.entity)
      jsonFieldsPartial <- fieldsToJsonFields(fields.zip(fieldsFile).zip(columns), lang)
    } yield {


      //to force adding primary keys if not specified by the user
      val missingKeyFields = keys.filterNot(k => fields.map(_._1.name).contains(k)).map{ key =>
        JSONField("string",key,false)
      }

      println(s"Missing Key fields $missingKeyFields")

      val definedTableFields = form.tabularFields.toSeq.flatMap(_.split(","))
      val missingKeyTableFields = keys.filterNot(k => definedTableFields.contains(k))
      val tableFields = missingKeyTableFields ++ definedTableFields

      val defaultQuery: Option[JSONQuery] = for{
        q <- form.query
        json <- parse(q).right.toOption
        jsonQuery <- json.as[JSONQuery].right.toOption
      } yield jsonQuery


      val jsonFields = {missingKeyFields ++ jsonFieldsPartial}.distinct

      val layout = form.layout.map { l =>
        parse(l).fold({ f =>
          println(f.getMessage())
          None
        }, { json =>
          json.as[Layout].fold({ f =>
            println(f.getMessage())
            None
          }, { lay =>
            Some(lay)
          }
          )
        })
      }.flatten.getOrElse(Layout.fromFields(jsonFields))



      val result = JSONMetadata(form.form_id.get,form.name,jsonFields,layout,form.entity,lang,tableFields,keys,defaultQuery)
      //println(s"resulting form: $result")
      result
    }

  }


  private def columns(form:Form_row,field:Field_row): Future[Option[PgColumn]] = {
    val pgColumns = TableQuery[PgColumns]
    Auth.adminDB.run{
      pgColumns.filter(row => row.column_name === field.name && row.table_name === form.entity).result
    }.map(_.headOption)
  }

  private def fieldsToJsonFields(fields:Seq[(((Field_row,Field_i18n_row),Option[FieldFile_row]),Option[PgColumn])], lang:String): Future[Seq[JSONField]] = {
    val jsonFields = fields.map{ case (((field,fieldI18n),fieldFile),pgColumn) =>
      val options: Option[Future[JSONFieldLookup]] = for{
        refEntity <- field.lookupEntity
        value <- field.lookupValueField
        text = fieldI18n.lookupTextField.getOrElse(lang)
      } yield {

        EntityActionsRegistry.tableActions(refEntity).getEntity().map{ lookupData =>   //JSONQuery.limit(100)
          val options = lookupData.map{ lookupRow =>
            (lookupRow.get(value),lookupRow.get(text))
          }.toMap
          JSONFieldLookup(refEntity, JSONFieldMap(value,text),options)
        }
      }



      import io.circe.generic.auto._

      val queryFilter:Seq[JSONQueryFilter] = {for{
        filter <- field.childFilter
        json <- parse(filter).right.toOption
        result <- json.as[Seq[JSONQueryFilter]].right.toOption
      } yield result }.toSeq.flatten


      val subform = for{
        id <- field.child_form_id
        local <- field.masterFields
        remote <- field.childFields
      } yield Child(id,field.name,local,remote,queryFilter)


      val nullable = pgColumn.map(_.nullable).getOrElse(true)

      val file = fieldFile.map{ ff =>
        FieldFile(ff.file_field,ff.name_field,ff.thumbnail_field)
      }

      options match {
        case Some(opt) => opt.map{ o =>
          JSONField(field.`type`, field.name, nullable, fieldI18n.label,Some(o), fieldI18n.placeholder, field.widget, subform, field.default,file)
        }
        case None => Future.successful{
          JSONField(field.`type`, field.name, nullable, fieldI18n.label,None, fieldI18n.placeholder, field.widget, subform, field.default,file)
        }
      }



    }

    Future.sequence(jsonFields)

  }

}
