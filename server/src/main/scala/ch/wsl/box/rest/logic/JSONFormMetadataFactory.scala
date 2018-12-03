package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.boxentities.Field.{FieldFile_row, Field_i18n_row, Field_row}
import ch.wsl.box.rest.boxentities.Form.{Form, Form_i18n, Form_i18n_row, Form_row}
import ch.wsl.box.rest.boxentities.{Field, Form}
import ch.wsl.box.rest.utils.{Auth, UserProfile}
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future, Promise}
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import scribe.Logging

import scala.util.{Failure, Success, Try}

/**
  * Created by andreaminetti on 10/03/16.
  *
  * mapping from form specs in box schema into JSONForm
  */
object JSONFormMetadataFactory{
  private var cacheName = Map[(String, String, String),Future[JSONMetadata]]()
  private var cacheId = Map[(String, Int, String),Future[JSONMetadata]]()

  def resetCache() = {
    cacheName = Map()
    cacheId = Map()
  }
}

case class JSONFormMetadataFactory(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext) extends Logging {

  implicit val db = up.db

  def list: Future[Seq[String]] = Auth.boxDB.run{
    Form.table.result
  }.map{_.map(_.name)}

  import ch.wsl.box.shared.utils.JSONUtils._

  def of(id:Int, lang:String):Future[JSONMetadata] = {
    JSONFormMetadataFactory.cacheId.lift((up.name, id,lang)) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata cache miss! cache key: ($id,$lang), cache: ${JSONFormMetadataFactory.cacheName}")
        val formQuery: Query[Form, Form_row, Seq] = for {
          form <- Form.table if form.form_id === id
        } yield form
        val result = getForm(formQuery,lang)
        JSONFormMetadataFactory.cacheId = JSONFormMetadataFactory.cacheId ++ Map((up.name, id,lang) -> result)
        result
      }
    }
  }

  def of(name:String, lang:String):Future[JSONMetadata] = {
    JSONFormMetadataFactory.cacheName.lift((up.name, name,lang)) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata cache miss! cache key: ($name,$lang), cache: ${JSONFormMetadataFactory.cacheName}")
        val formQuery: Query[Form, Form_row, Seq] = for {
          form <- Form.table if form.name === name
        } yield form
        val result = getForm(formQuery,lang)
        JSONFormMetadataFactory.cacheName = JSONFormMetadataFactory.cacheName ++ Map((up.name, name,lang) -> result)
        result
      }
    }

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
      (field,fieldI18n) <- Field.table joinLeft Field.Field_i18n.filter(_.lang === lang) on(_.field_id === _.field_id) if field.form_id === formId
    } yield (field,fieldI18n)

    val fQuery = formQuery joinLeft Form.Form_i18n.filter(_.lang === lang) on (_.form_id === _.form_id)


    for{
      (form,formI18n) <- Auth.boxDB.run( fQuery.result ).map(_.head)
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

      logger.info(s"Missing Key fields $missingKeyFields")

      if(formI18n.isEmpty) logger.warn(s"Form ${form.name} (form_id: ${form.form_id}) has no translation to $lang")

      val definedTableFields = form.tabularFields.toSeq.flatMap(_.split(","))
      val missingKeyTableFields = keys.filterNot(k => definedTableFields.contains(k))
      val tableFields = missingKeyTableFields ++ definedTableFields

      val defaultQuery: Option[JSONQuery] = for{
        q <- form.query
        json <- parse(q).right.toOption
        jsonQuery <- json.as[JSONQuery].right.toOption
      } yield jsonQuery


      val jsonFields = {missingKeyFields ++ jsonFieldsPartial}.distinct

      def defaultLayout:Layout = { // for subform default with 12
        val default = Layout.fromFields(jsonFields)
        default.copy(blocks = default.blocks.map(_.copy(width = 12)))
      }

      val layout = Layout.fromString(form.layout).getOrElse(defaultLayout)



      val result = JSONMetadata(form.form_id.get,form.name,formI18n.flatMap(_.label).getOrElse(form.name),jsonFields,layout,form.entity,lang,tableFields,keys,defaultQuery, form.exportFields.map(_.split(",").toSeq).getOrElse(tableFields))//, form.entity)
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

  private def fieldsToJsonFields(fields:Seq[(((Field_row,Option[Field_i18n_row]),Option[FieldFile_row]),Option[PgColumn])],lang:String): Future[Seq[JSONField]] = {

    val jsonFields = fields.map{ case (((field,fieldI18n),fieldFile),pgColumn) =>

      if(fieldI18n.isEmpty) logger.warn(s"Field ${field.name} (field_id: ${field.field_id}) has no translation to $lang")

      val lookup: Future[Option[JSONFieldLookup]] = {for{
        refEntity <- field.lookupEntity
        value <- field.lookupValueField

        text = fieldI18n.flatMap(_.lookupTextField).getOrElse(JSONMetadataFactory.lookupField(refEntity,lang,None))
      } yield {

        import io.circe.generic.auto._
        for{
          keys <- JSONMetadataFactory.keysOf(refEntity)
          filter = { for{
            queryString <- field.lookupQuery
            queryJson <- parse(queryString).right.toOption
            query <- queryJson.as[JSONQuery].right.toOption
          } yield query }.getOrElse(JSONQuery.sortByKeys(keys))

          lookupData <- EntityActionsRegistry().tableActions(refEntity).find(filter)

        } yield {
//          val options = lookupData.map{ lookupRow =>
//            JSONLookup(lookupRow.get(value),lookupRow.get(text))
//          }
//          Some(JSONFieldLookup(refEntity, JSONFieldMap(value,text),options))
          Some(JSONFieldLookup.fromData(refEntity, JSONFieldMap(value,text), lookupData))
        }

      }} match {
        case Some(a) => a
        case None => Future.successful(None)
      }

      import io.circe.generic.auto._

      val queryFilter:Seq[JSONQueryFilter] = {for{
        filter <- field.childFilter
        json <- parse(filter).right.toOption
        result <- json.as[Seq[JSONQueryFilter]].right.toOption
      } yield
        result }.toSeq.flatten


      val subform = for{
        id <- field.child_form_id
        local <- field.masterFields
        remote <- field.childFields
      } yield Child(id,field.name,local,remote,queryFilter)


      val label:Future[String] = {

        field.child_form_id match {
          case None => Future.successful(fieldI18n.flatMap(_.label).getOrElse(field.name))
          case Some(subformId) => Auth.boxDB.run{
            {
              for{
                (form,formI18n) <- Form.table joinLeft Form_i18n.filter(_.lang === lang) on (_.form_id === _.form_id) if form.form_id === subformId
              } yield (formI18n,form)
            }.result.map{x => x.head._1.flatMap(_.label).getOrElse(x.head._2.name)}
          }
        }
      }

      val tooltip:Future[Option[String]] = Future.successful(fieldI18n.flatMap(_.tooltip))

      val placeholder:Future[Option[String]] = Future.successful(fieldI18n.flatMap(_.placeholder))

      val nullable = pgColumn.map(_.nullable).getOrElse(true)

      val file = fieldFile.map{ ff =>
        FileReference(ff.name_field, ff.file_field, ff.thumbnail_field)
      }


      val condition = for{
        fieldId <- field.conditionFieldId
        values <- field.conditionValues
        json <- Try(parse(values).right.get.as[Seq[Json]].right.get).toOption
      } yield ConditionalField(fieldId,json)

      for{
        look <- lookup
        lab <- label
        placeHolder <- placeholder
        tip <- tooltip
      } yield {
        JSONField(field.`type`, field.name, nullable, Some(lab),look, placeHolder, field.widget, subform, field.default,file,condition,tip)
      }

    }

    Future.sequence(jsonFields)

  }

}
