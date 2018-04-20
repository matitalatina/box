package ch.wsl.box.rest.logic

import akka.stream.Materializer
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
  }.map{_.map(_.name)}

  def of(name:String, lang:String):Future[JSONMetadata]  = {
    val query = for{
      export <- Export.Export if export.name === name
      exportI18n <- Export.Export_i18n if exportI18n.lang === lang && exportI18n.export_id === export.export_id
    } yield (export,exportI18n)

    def queryField(exportId:Int) = for{
      fields <- ExportField.ExportField join ExportField.ExportField_i18n on (_.field_id === _.field_id) if fields._1.export_id === exportId
    } yield fields

    for {
      (export,exportI18n) <- Auth.boxDB.run {
        query.result
      }.map(_.head)
      fields <- Auth.boxDB.run {
        queryField(export.export_id.get).result
      }
    } yield {

      val jsonFields = fields.map(fieldsMetadata)

      val layout = Layout.fromString(export.layout).getOrElse(Layout.fromFields(jsonFields))

      val parameters = export.parameters.toSeq.flatMap(_.split(","))

      JSONMetadata(export.export_id.get,export.name,exportI18n.label.getOrElse(name),jsonFields,layout,exportI18n.function.getOrElse(export.function),lang,parameters,Seq(),None,None)
    }
  }

  private def fieldsMetadata(el:(ExportField_row,ExportField_i18n_row)):JSONField = {
    val (field,fieldI18n) = el

    val lookup = for{
      entity <- field.lookupEntity
      value <- field.lookupValueField
      text <- fieldI18n.lookupTextField
    } yield JSONFieldLookup(entity,JSONFieldMap(value,text))

    val condition = for{
      fieldId <- field.conditionFieldId
      values <- field.conditionValues
      json <- Try(parse(values).right.get.as[Seq[Json]].right.get).toOption
    } yield ConditionalField(fieldId,json)

    JSONField(
      field.`type`,
      field.name,
      true,
      fieldI18n.label,
      lookup,
      fieldI18n.placeholder,
      field.widget,
      None,
      field.default,
      None,
      condition
    )

  }

}
