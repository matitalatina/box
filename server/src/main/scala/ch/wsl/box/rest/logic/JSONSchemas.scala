package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.{JSONSchema, JSONSchemaL2, WidgetsNames}
import ch.wsl.box.rest.utils.Auth

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

    val schema = new PgInformationSchema(table,db,Auth.dbSchema)

    val prop:Future[Map[String,JSONSchemaL2]] = schema.columns.map(_.map(properties(_)).toMap)  //first map on future, second on columns

    println("columns")

    for{
      p <- prop
      c <- schema.columns
    } yield {
      JSONSchema(
        `type` = "object",
        title = Some(table),
        properties = p,
        readonly = Some(c.forall { x => !x.updatable }),
        required = Some(c.filter(!_.nullable).map(_.boxName))
      )
    }
  }



  def keysOf(table:String):Future[Seq[String]] = {
    println("Getting " + table + " keys")
    new PgInformationSchema(table,Auth.adminDB).pk.map { pk =>   //map to enter the future
      println(pk)
      pk.boxKeys
    }

  }


  def properties(c:PgColumn):(String, JSONSchemaL2) = {
    (c.boxName -> JSONSchemaL2(c.jsonType,Some(c.boxName),order=Some(c.ordinal_position),readonly=Some(!c.updatable)))

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

  val defaultWidgetMapping = Map(
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