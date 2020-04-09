package ch.wsl.box.rest.metadata

import akka.stream.Materializer
import ch.wsl.box.model.boxentities.Field.{FieldFile_row, Field_i18n_row, Field_row}
import ch.wsl.box.model.boxentities.Form.{Form, Form_i18n, Form_row}
import ch.wsl.box.model.boxentities.{Field, Form}
import ch.wsl.box.model.shared._
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.rest.utils.{Auth, BoxConf, UserProfile}
import io.circe._
import io.circe.parser._
import scribe.Logging

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by andreaminetti on 10/03/16.
  *
  * mapping from form specs in box schema into JSONForm
  */
object FormMetadataFactory{
  /**
   * Caches
   * cache keys contains lang identifier of the form (id or name) and username,
   * username it's crucial to avoid exposing rows that are not accessible by the user (in foreign keys)
   *
   * cache should be resetted when an external field changes
   */
  private var cacheName = Map[(String, String, String),Future[JSONMetadata]]()
  private var cacheId = Map[(String, Int, String),Future[JSONMetadata]]()

  def resetCache() = {
    cacheName = Map()
    cacheId = Map()
  }

  def resetCacheForEntity(e:String) = {
    cacheId = cacheId.filterNot(c => CacheUtils.checkIfHasForeignKeys(e, c._2))
    cacheName = cacheName.filterNot(c => CacheUtils.checkIfHasForeignKeys(e, c._2))
  }

}



case class FormMetadataFactory(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext) extends Logging with MetadataFactory {

  implicit val db = up.db

  def list: Future[Seq[String]] = Auth.boxDB.run{
    Form.table.result
  }.map{_.map(_.name)}

  def of(id:Int, lang:String):Future[JSONMetadata] = {
    FormMetadataFactory.cacheId.lift((up.name, id,lang)) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata cache miss! cache key: ($id,$lang), cache: ${FormMetadataFactory.cacheName}")
        val formQuery: Query[Form, Form_row, Seq] = for {
          form <- Form.table if form.form_id === id
        } yield form
        val result = getForm(formQuery,lang)
        if(BoxConf.enableCache) FormMetadataFactory.cacheId = FormMetadataFactory.cacheId ++ Map((up.name, id,lang) -> result)
        result
      }
    }
  }

  def of(name:String, lang:String):Future[JSONMetadata] = {
    FormMetadataFactory.cacheName.lift((up.name, name,lang)) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata cache miss! cache key: ($name,$lang), cache: ${FormMetadataFactory.cacheName}")
        val formQuery: Query[Form, Form_row, Seq] = for {
          form <- Form.table if form.name === name
        } yield form
        val result = getForm(formQuery,lang)
        if(BoxConf.enableCache) FormMetadataFactory.cacheName = FormMetadataFactory.cacheName ++ Map((up.name, name,lang) -> result)
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
      keys <- EntityMetadataFactory.keysOf(form.entity)
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

        text = fieldI18n.flatMap(_.lookupTextField).getOrElse(EntityMetadataFactory.lookupField(refEntity,lang,None))
      } yield {

        import io.circe.generic.auto._
        for{
          keys <- EntityMetadataFactory.keysOf(refEntity)
          filter = { for{
            queryString <- field.lookupQuery
            queryJson <- parse(queryString).right.toOption
            query <- queryJson.as[JSONQuery].right.toOption
          } yield query }.getOrElse(JSONQuery.sortByKeys(keys))

          lookupData <- Registry().actions.actions(refEntity)(ec).get.find(filter)

        } yield {
//          val options = lookupData.map{ lookupRow =>
//            JSONLookup(lookupRow.get(value),lookupRow.get(text))
//          }
//          Some(JSONFieldLookup(refEntity, JSONFieldMap(value,text),options))
          Some(JSONFieldLookup.fromData(refEntity, JSONFieldMap(value,text), lookupData,field.lookupQuery))
        }

      }} match {
        case Some(a) => a
        case None => Future.successful(None)
      }

      import io.circe.generic.auto._

      val childQuery:Option[JSONQuery] = {for{
        filter <- field.childQuery
        json <- parse(filter).right.toOption
        result <- json.as[JSONQuery].right.toOption
      } yield
        result }

      (field.childQuery,childQuery) match {
        case (Some(f),None) => logger.warn(s"$f not parsed correctly")
        case _ => {}
      }


      val subform = for{
        id <- field.child_form_id
        local <- field.masterFields
        remote <- field.childFields
      } yield {
        Child(id,field.name,local,remote,childQuery)
      }


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
