package ch.wsl.rest.domain

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.direct._

case class JSONSchema(
  `type`:String,
  title:Option[String] = None,
  properties: Option[Map[String,JSONSchema]] = None,
  required: Option[List[String]] = None,
  enum: Option[List[String]] = None
)


object JSONSchema extends ProductionDB {
  
  val pgColumns = TableQuery[PgColumns]
  
  def of(table:String):JSONSchema = {
    
    val columns = db withSession { implicit s =>
      pgColumns
        .filter(e => e.table_name === table && e.table_schema === "public")
        .sortBy(_.ordinal_position)
        .list
    }
    
    
    JSONSchema(
        `type` = "object",
        title = Some(table),
        properties = Some(properties(columns)),
        required = Some(columns.filter(_.is_nullable == "NO").map(_.column_name))
    )
  }
  
  def properties(columns:List[PgColumn]):Map[String,JSONSchema] = {
    
    {for{
      c <- columns
    } yield {
      c.column_name -> JSONSchema(typesMapping(c.data_type),Some(c.column_name))
    }}.toMap
    
  }
  
  val typesMapping =  Map(
      "integer" -> "number",
      "character varying" -> "string",
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
      "USER-DEFINED" -> "string"

  )
  
}