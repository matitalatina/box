package ch.wsl.rest.domain

import scala.collection.immutable.ListMap


case class JSONSchema(
  `type`:String,
  title:Option[String] = None,
  properties: Option[ListMap[String,JSONSchema]] = None,
  required: Option[List[String]] = None,
  readonly: Option[Boolean] = None,
  enum: Option[List[String]] = None,
  order: Option[Int] = None
)


object JSONSchema {
    
  def of(table:String):JSONSchema = {
    
    val schema = new PgSchema(table)
    
    val map = ListMap(properties(schema.columns): _*)
    
    println(schema.columns)
    
    JSONSchema(
        `type` = "object",
        title = Some(table),
        properties = Some(map),
        readonly = Some(schema.columns.forall { x => x.is_updatable == "NO" }),
        required = Some(schema.columns.filter(_.is_nullable == "NO").map(_.column_name))
    )
  }
  
  def keysOf(table:String):List[String] = new PgSchema(table).pk
  
  
  def properties(columns:List[PgColumn]):List[(String,JSONSchema)] = {
    
    val cols = {for{
      c <- columns
    } yield {
      c.column_name -> JSONSchema(typesMapping(c.data_type),Some(c.column_name),order=Some(c.ordinal_position),readonly=Some(c.is_updatable == "NO"))
    }}.toList
    
    
    cols
  }
  
  val typesMapping =  Map(
      "integer" -> "number",
      "character varying" -> "text",
      "character" -> "text",
      "smallint" -> "number",
      "bigint" -> "number",
      "double precision" -> "number",
      "timestamp without time zone" -> "text",
      "date" -> "text",
      "real" -> "number",
      "boolean" -> "checkbox",
      "bytea" -> "text",
      "numeric" -> "number",
      "text" -> "text",
      "USER-DEFINED" -> "text"

  )
  
}