package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.boxentities.ExportField.{ExportField_i18n_row, ExportField_row}
import ch.wsl.box.rest.boxentities.{Export, ExportField, Form}
import ch.wsl.box.rest.utils.Auth
import io.circe.Json
import io.circe.parser.parse
import scribe.Logging
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class JSONExportMetadataFactory(implicit db:Database, mat:Materializer, ec:ExecutionContext) extends Logging {

  import io.circe.generic.auto._


  def list: Future[Seq[String]] = Auth.boxDB.run{
    Export.Export.result
  }.map{_.sortBy(_.order.getOrElse(Double.MaxValue)).map(_.name)}

  def list(lang:String): Future[Seq[ExportDef]] = {
    val query = for {
        (e, ei18) <- Export.Export joinLeft(Export.Export_i18n.filter(_.lang === lang)) on(_.export_id === _.export_id)

  } yield (ei18.flatMap(_.label), e.function, e.name, e.order, ei18.flatMap(_.hint), ei18.flatMap(_.tooltip))

    Auth.boxDB.run{
      query.result
    }.map(_.sortBy(_._4.getOrElse(Double.MaxValue))).map(_.map{ case (label, function, name, _, hint, tooltip) =>
            ExportDef(function, label.getOrElse(name), hint, tooltip)
    })
  }

  def defOf(function:String, lang:String): Future[ExportDef] = {
    val query = for {
      (e, ei18) <- Export.Export joinLeft(Export.Export_i18n.filter(_.lang === lang)) on(_.export_id === _.export_id)
      if e.function === function

    } yield (ei18.flatMap(_.label), e.function, e.name, e.order, ei18.flatMap(_.hint), ei18.flatMap(_.tooltip))

    Auth.boxDB.run{
      query.result
    }.map(_.map{ case (label, function, name, _, hint, tooltip) =>
      ExportDef(function, label.getOrElse(name), hint, tooltip)
    }.head)
  }

  def of(function:String, lang:String):Future[JSONMetadata]  = {
    val queryExport = for{
      (export, exportI18n) <- Export.Export joinLeft Export.Export_i18n.filter(_.lang === lang) on (_.export_id === _.export_id)
      if export.function === function

    } yield (export,exportI18n)

    def queryField(exportId:Int) = for{
      (f, fi18n) <- ExportField.ExportField joinLeft ExportField.ExportField_i18n.filter(_.lang === lang) on (_.field_id === _.field_id)
                if f.export_id === exportId
    } yield (f, fi18n)

    for {
      (export,exportI18n) <- Auth.boxDB.run {
        queryExport.result
      }.map(_.head)

      fields <- Auth.boxDB.run {
        queryField(export.export_id.get).sortBy(_._1.field_id).result
      }

      jsonFields <- Future.sequence(fields.map(fieldsMetadata(lang)))

    } yield {

      if(exportI18n.isEmpty) logger.warn(s"Export ${export.name} (export_id: ${export.export_id}) has no translation to $lang")


//      val jsonFields = fields.map(fieldsMetadata(lang))

//      def defaultLayout:Layout = { // for subform default with 12
//        val default = Layout.fromFields(jsonFields)
//        default.copy(blocks = default.blocks.map(_.copy(width = 12)))
//      }

      val layout = Layout.fromString(export.layout).getOrElse(Layout.fromFields(jsonFields))

      val parameters = export.parameters.toSeq.flatMap(_.split(","))

      JSONMetadata(export.export_id.get,export.name,exportI18n.flatMap(_.label).getOrElse(function),jsonFields,layout,exportI18n.flatMap(_.function).getOrElse(export.function),lang,parameters,Seq(),None,Seq())//,"")
    }
  }

  private def fieldsMetadata(lang:String)(el:(ExportField_row, Option[ExportField_i18n_row])):Future[JSONField] = {
    import ch.wsl.box.shared.utils.JSONUtils._
    import io.circe.generic.auto._

    val (field,fieldI18n) = el

    if(fieldI18n.isEmpty) logger.warn(s"Export field ${field.name} (export_id: ${field.field_id}) has no translation to $lang")


    val lookup: Future[Option[JSONFieldLookup]] = {for{
      entity <- field.lookupEntity
      value <- field.lookupValueField
      text <- fieldI18n.flatMap(_.lookupTextField)

    } yield {
      import io.circe.generic.auto._
      for {

        keys <- JSONMetadataFactory.keysOf(entity)
        filter = { for{
          queryString <- field.lookupQuery
          queryJson <- parse(queryString).right.toOption
          query <- queryJson.as[JSONQuery].right.toOption
        } yield query }.getOrElse(JSONQuery.sortByKeys(keys))

        lookupData <- EntityActionsRegistry().tableActions(entity).find(filter)

      } yield {
        Some(JSONFieldLookup.fromData(entity, JSONFieldMap(value, text), lookupData))
      }
    }} match {
        case Some(a) => a
        case None => Future.successful(None)
    }

    val condition = for{
      fieldId <- field.conditionFieldId
      values <- field.conditionValues
      json <- Try(parse(values).right.get.as[Seq[Json]].right.get).toOption
    } yield ConditionalField(fieldId,json)


    for{
      look <- lookup
//      lab <- label
//      placeHolder <- placeholder
//      tip <- tooltip
    } yield {
      JSONField(
        field.`type`,
        field.name,
        false,
        fieldI18n.flatMap(_.label),
        look,
        fieldI18n.flatMap(_.placeholder),
        field.widget,
        None,
        field.default,
        None,
        condition
        //      fieldI18n.flatMap(_.tooltip)
      )
    }

  }

}
