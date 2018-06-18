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
  }.map{_.sortBy(_.order.getOrElse(Double.MaxValue)).map(_.name)}

  def list(lang:String): Future[Seq[ExportDef]] = {
    val query = for {
        (e, ei18) <- Export.Export joinLeft(Export.Export_i18n.filter(_.lang === lang)) on(_.export_id === _.export_id)

  } yield (ei18.flatMap(_.label), e.name, e.order)

    Auth.boxDB.run{
      query.result
    }.map(_.sortBy(_._3.getOrElse(Double.MaxValue))).map(_.map{ case (label,name,_) =>  ExportDef(name, label.getOrElse(name))})
  }

  def of(name:String, lang:String):Future[JSONMetadata]  = {
    val queryExport = for{
      (export, exportI18n) <- Export.Export joinLeft Export.Export_i18n.filter(_.lang === lang) on (_.export_id === _.export_id) if export.name === name

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
        val r = queryField(export.export_id.get).result
        println(r.statements.mkString("\n"))
        r
      }
    } yield {

      val jsonFields = fields.map(fieldsMetadata)

      val layout = Layout.fromString(export.layout).getOrElse(Layout.fromFields(jsonFields))

      val parameters = export.parameters.toSeq.flatMap(_.split(","))

      JSONMetadata(export.export_id.get,export.name,exportI18n.flatMap(_.label).getOrElse(name),jsonFields,layout,exportI18n.flatMap(_.function).getOrElse(export.function),lang,parameters,Seq(),None,None,"")
    }
  }

  private def fieldsMetadata(el:(ExportField_row, Option[ExportField_i18n_row])):JSONField = {
    val (field,fieldI18n) = el

    val lookup = for{
      entity <- field.lookupEntity
      value <- field.lookupValueField
      text <- fieldI18n.flatMap(_.lookupTextField)
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
      fieldI18n.flatMap(_.label),
      lookup,
      fieldI18n.flatMap(_.placeholder),
      field.widget,
      None,
      field.default,
      None,
      condition
    )

  }

}
