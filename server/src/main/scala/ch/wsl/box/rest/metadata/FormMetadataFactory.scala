package ch.wsl.box.rest.metadata

import akka.stream.Materializer
import ch.wsl.box.information_schema.{PgColumn, PgColumns, PgInformationSchema}
import ch.wsl.box.jdbc.{FullDatabase, UserDatabase}
import ch.wsl.box.model.boxentities.BoxField.{BoxFieldFile_row, BoxField_i18n_row, BoxField_row}
import ch.wsl.box.model.boxentities.BoxForm.{BoxFormTable, BoxForm_i18nTable, BoxForm_row}
import ch.wsl.box.model.boxentities.{BoxField, BoxForm}
import ch.wsl.box.model.shared._
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.rest.utils.{Auth, BoxConfig, UserProfile}
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
  private var cacheFormName = Map[(String, String, String),Future[JSONMetadata]]()   //(up.name, form id, lang)
  private var cacheFormId = Map[(String, Int, String),Future[JSONMetadata]]()        //(up.name, from name, lang)

  def resetCache() = {
    cacheFormName = Map()
    cacheFormId = Map()
  }

  def resetCacheForEntity(e:String) = {
    cacheFormId = cacheFormId.filterNot(c => CacheUtils.checkIfHasForeignKeys(e, c._2))
    cacheFormName = cacheFormName.filterNot(c => CacheUtils.checkIfHasForeignKeys(e, c._2))
  }

  def hasGuestAccess(formName:String,adminDb:UserDatabase)(implicit ec:ExecutionContext):Future[Option[UserProfile]] = adminDb.run{
    BoxFormTable.filter(f => f.name === formName && f.guest_user.nonEmpty).result.headOption
  }.map{_.map{ form =>
    Auth.userProfileForUser(form.guest_user.get)
  }}


}



case class FormMetadataFactory(adminDb:UserDatabase)(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext) extends Logging with MetadataFactory {



  def list: Future[Seq[String]] = adminDb.run{
    BoxForm.BoxFormTable.result
  }.map{_.map(_.name)}

  def of(id:Int, lang:String):Future[JSONMetadata] = {
    val cacheKey = (up.name, id,lang)
    FormMetadataFactory.cacheFormId.lift(cacheKey) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata cache miss! cache key: ($id,$lang), cache: ${FormMetadataFactory.cacheFormName}")
        val formQuery: Query[BoxForm.BoxForm, BoxForm_row, Seq] = for {
          form <- BoxForm.BoxFormTable if form.form_id === id
        } yield form
        val result = adminDb.run(getForm(formQuery,lang))
        if(BoxConfig.enableCache) FormMetadataFactory.cacheFormId = FormMetadataFactory.cacheFormId ++ Map(cacheKey -> result)
        result.onComplete{ x =>
          if(x.isFailure) {
            FormMetadataFactory.cacheFormId = FormMetadataFactory.cacheFormId.filterKeys(_ != cacheKey)
          }
        }
        result
      }
    }
  }

  def of(name:String, lang:String):Future[JSONMetadata] = {
    val cacheKey = (up.name, name,lang)
    FormMetadataFactory.cacheFormName.lift(cacheKey) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata cache miss! cache key: ($name,$lang), cache: ${FormMetadataFactory.cacheFormName}")
        val formQuery: Query[BoxForm.BoxForm, BoxForm_row, Seq] = for {
          form <- BoxForm.BoxFormTable if form.name === name
        } yield form
        val result = adminDb.run(getForm(formQuery,lang))
        if(BoxConfig.enableCache) FormMetadataFactory.cacheFormName = FormMetadataFactory.cacheFormName ++ Map(cacheKey -> result)
        result.onComplete{x =>
          if(x.isFailure) {
            FormMetadataFactory.cacheFormName = FormMetadataFactory.cacheFormName.filterKeys(_ != cacheKey)
          }
        }
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

  private def keys(form:BoxForm_row):Future[Seq[String]] = form.edit_key_field.map{x =>
    Future.successful(x.split(",").toSeq.map(_.trim))
  }.getOrElse(EntityMetadataFactory.keysOf(Auth.dbSchema,form.entity))

  private def getForm(formQuery: Query[BoxForm.BoxForm,BoxForm_row,Seq], lang:String) = {

    import io.circe.generic.auto._

    def fieldQuery(formId:Int) = for{
      (field,fieldI18n) <- BoxField.BoxFieldTable joinLeft BoxField.BoxField_i18nTable.filter(_.lang === lang) on(_.field_id === _.field_id) if field.form_id === formId
    } yield (field,fieldI18n)

    val fQuery = formQuery joinLeft BoxForm.BoxForm_i18nTable.filter(_.lang === lang) on (_.form_id === _.form_id)


    val result = for{
      (form,formI18n) <- fQuery.result.map(_.head)
      fields <- fieldQuery(form.form_id.get).result
      fieldsFile <- DBIO.sequence(fields.map { case (f, _) =>
          BoxField.BoxFieldFileTable.filter(_.field_id === f.field_id).result.headOption
      })
      actions <- BoxForm.BoxForm_actions.filter(_.form_id === form.form_id.get).result

      cols <- DBIO.from(new PgInformationSchema(Auth.dbSchema,form.entity)(ec,adminDb).columns)
      columns = fields.map(f => cols.find(_.column_name == f._1.name))
      keys <- DBIO.from(keys(form))
      jsonFieldsPartial <- DBIO.from(fieldsToJsonFields(fields.zip(fieldsFile).zip(columns), lang))
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

      val formActions = if(actions.isEmpty) {
        FormActionsMetadata.default
      } else {
        FormActionsMetadata(
          actions = actions.map{a =>
            FormAction(
              action = Action.fromString(a.action),
              importance = Importance.fromString(a.importance),
              afterActionGoTo = a.after_action_goto,
              label = a.label,
              updateOnly = a.update_only,
              insertOnly = a.insert_only,
              reload = a.reload,
              confirmText = a.confirm_text
            )
          },
          navigationActions = Seq() // TODO
        )
      }

      val result = JSONMetadata(
        form.form_id.get,
        form.name,
        formI18n.flatMap(_.label).getOrElse(form.name),
        jsonFields,
        layout,
        form.entity,
        lang,
        tableFields,
        form.tabularFields.toSeq.flatMap(_.split(",")),
        keys,
        defaultQuery,
        form.exportFields.map(_.split(",").toSeq).getOrElse(tableFields),
        formI18n.flatMap(_.viewTable),
        formActions
      )//, form.entity)
      //println(s"resulting form: $result")
      result
    }

    result.transactionally

  }

  private def linkedForms(field:BoxField_row):Future[Option[LinkedForm]] = {
    val linkedFormOpt = for{
      formId <- field.child_form_id
      parentFields <- field.linked_key_fields
      childFields <- field.childFields
      parentLabel <- field.linked_label_fields
    } yield {
      adminDb.run{
        BoxForm.BoxFormTable.filter(_.form_id === formId).result
      }.map{ lForm =>
        lForm.map{ value =>
          LinkedForm(
            value.name,
            parentFields.split(",").map(_.trim),
            childFields.split(",").map(_.trim),
            parentLabel.split(",").map(_.trim),
          )
        }
      }
    }

    Future.sequence(linkedFormOpt.toSeq).map(_.flatten.headOption) // fix types
  }

  private def condition(field:BoxField_row) = for{
    fieldId <- field.conditionFieldId
    values <- field.conditionValues
    json <- Try(parse(values).right.get.as[Seq[Json]].right.get).toOption
  } yield ConditionalField(fieldId,json)

  private def file(ff:BoxFieldFile_row) = FileReference(ff.name_field, ff.file_field, ff.thumbnail_field)

  private def label(field:BoxField_row,fieldI18n:Option[BoxField_i18n_row], lang:String):Future[String] = {

    field.child_form_id match {
      case None => Future.successful(fieldI18n.flatMap(_.label).getOrElse(field.name))
      case Some(subformId) => adminDb.run{
        {
          for{
            (form,formI18n) <- BoxForm.BoxFormTable joinLeft BoxForm_i18nTable.filter(_.lang === lang) on (_.form_id === _.form_id) if form.form_id === subformId
          } yield (formI18n,form)
        }.result.map{x => x.head._1.flatMap(_.label).getOrElse(x.head._2.name)}
      }
    }
  }

  private def subform(field:BoxField_row) = field.`type` match {
    case JSONFieldTypes.CHILD => {

      import io.circe.generic.auto._

      val childQuery:Option[JSONQuery] = for{
        filter <- field.childQuery
        json <- parse(filter).right.toOption
        result <- json.as[JSONQuery].right.toOption
      } yield result

      (field.childQuery,childQuery) match {
        case (Some(f),None) => logger.warn(s"$f not parsed correctly")
        case _ => {}
      }

      for{
        id <- field.child_form_id
        local <- field.masterFields
        remote <- field.childFields
      } yield {
        Child(id,field.name,local,remote,childQuery)
      }
    }
    case _ => None
  }

  private def lookup(field:BoxField_row,fieldI18n:Option[BoxField_i18n_row], lang:String): Future[Option[JSONFieldLookup]] = {for{
    refEntity <- field.lookupEntity
    value <- field.lookupValueField

    text = fieldI18n.flatMap(_.lookupTextField).getOrElse(EntityMetadataFactory.lookupField(refEntity,lang,None))
  } yield {

    import io.circe.generic.auto._

    implicit def fDb = FullDatabase(up.db,adminDb)

    for{
      keys <- EntityMetadataFactory.keysOf(Auth.dbSchema,refEntity)
      filter = { for{
        queryString <- field.lookupQuery
        queryJson <- parse(queryString).right.toOption
        query <- queryJson.as[JSONQuery].right.toOption
      } yield query }.getOrElse(JSONQuery.sortByKeys(keys))

      lookupData <- up.db.run(Registry().actions(refEntity).find(filter.copy(lang = Some(lang))))

    } yield {
      Some(JSONFieldLookup.fromData(refEntity, JSONFieldMap(value,text,field.masterFields.getOrElse(field.name)), lookupData,field.lookupQuery))
    }

  }} match {
    case Some(a) => a
    case None => Future.successful(None)
  }

  private def fieldsToJsonFields(fields:Seq[(((BoxField_row,Option[BoxField_i18n_row]),Option[BoxFieldFile_row]),Option[PgColumn])], lang:String): Future[Seq[JSONField]] = {

    val jsonFields = fields.map{ case (((field,fieldI18n),fieldFile),pgColumn) =>

      if(fieldI18n.isEmpty) logger.warn(s"Field ${field.name} (field_id: ${field.field_id}) has no translation to $lang")

      for{
        look <- lookup(field, fieldI18n, lang)
        lab <- label(field, fieldI18n, lang)
        linked <- linkedForms(field)
      } yield {
        JSONField(
          `type` = field.`type`,
          name = field.name,
          nullable = pgColumn.map(_.nullable).getOrElse(true),
          readOnly = field.read_only,
          label = Some(lab),
          lookup = look,
          placeholder = fieldI18n.flatMap(_.placeholder),
          widget = field.widget,
          child = subform(field),
          default = field.default,
          file = fieldFile.map(file),
          condition = condition(field),
          tooltip = fieldI18n.flatMap(_.tooltip),
          params = field.params,
          linked = linked
        )
      }

    }

    Future.sequence(jsonFields)

  }

}
