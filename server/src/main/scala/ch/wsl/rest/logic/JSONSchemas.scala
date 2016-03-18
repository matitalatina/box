package ch.wsl.rest.logic

import ch.wsl.box.model.shared.{JSONSchemaL2, JSONSchema}
import ch.wsl.rest.service.Auth

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andreaminetti on 10/03/16.
  */
object JSONSchemas {

  import StringHelper._

  def of(table:String,db:slick.driver.PostgresDriver.api.Database):Future[JSONSchema] = {

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
    new PgInformationSchema(table,Auth.adminDB).pk.map { pks =>
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
    "boolean" -> "checkbox",
    "bytea" -> "string",
    "numeric" -> "number",
    "text" -> "string",
    "USER-DEFINED" -> "string"
  )

}