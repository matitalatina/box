package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.{WidgetsNames, JSONSchemaL2, JSONSchema}
import ch.wsl.box.rest.service.Auth

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andreaminetti on 10/03/16.
  *
  * retrieves schema information from database (based query on information schema table  (catalog))
  */
object JSONSchemas {

  import StringHelper._

  def of(table:String)(implicit db:slick.driver.PostgresDriver.api.Database):Future[JSONSchema] = {

    println("Getting JSONSchema of:" + table)

    val schema = new PgInformationSchema(table,db)

    val prop:Future[Map[String,JSONSchemaL2]] = schema.columns.map{ col => properties(col) }

    println("columns")

    for{
      p <- prop
      c <- schema.columns
    } yield {
      JSONSchema(
        `type` = "object",
        title = Some(table),
        properties = p,
        readonly = Some(c.forall { x => x.is_updatable == "NO" }),
        required = Some(c.filter(_.is_nullable == "NO").map(_.column_name.slickfy))
      )
    }
  }



  def keysOf(table:String):Future[Seq[String]] = {
    println("Getting " + table + " keys")
    new PgInformationSchema(table,Auth.adminDB).pk.map { pks =>
      println(pks)
      pks.map(_.slickfy)
    }

  }


  def properties(columns:Seq[PgColumn]):Map[String,JSONSchemaL2] = {

    val cols = {for{
      c <- columns
    } yield {
      c.column_name.slickfy -> JSONSchemaL2(typesMapping(c.data_type),Some(c.column_name.slickfy),order=Some(c.ordinal_position),readonly=Some(c.is_updatable == "NO"))
    }}.toMap


    cols
  }

  val typesMapping =  Map(
    "integer" -> "number",
    "character varying" -> "string",
    "character" -> "string",
    "smallint" -> "number",
    "bigint" -> "number",
    "double precision" -> "number",
    "timestamp without time zone" -> "string",
    "date" -> "string",
    "real" -> "number",
    "boolean" -> "boolean",
    "bytea" -> "string",
    "numeric" -> "number",
    "text" -> "string",
    "USER-DEFINED" -> "string",
    "time without time zone" -> "string"
  )

  val widgetMapping = Map(
    "integer" -> None,
    "character varying" -> Some(WidgetsNames.textinput),
    "character" -> Some(WidgetsNames.textinput),
    "smallint" -> None,
    "bigint" -> None,
    "double precision" -> None,
    "timestamp without time zone" -> Some(WidgetsNames.datetimePicker),
    "date" -> Some(WidgetsNames.datepicker),
    "real" -> None,
    "boolean" -> None,
    "bytea" -> None,
    "numeric" -> None,
    "text" -> Some(WidgetsNames.textinput),
    "USER-DEFINED" -> None,
    "time without time zone" -> Some(WidgetsNames.timepicker)
  )

}