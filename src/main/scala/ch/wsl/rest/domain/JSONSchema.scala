package ch.wsl.rest.domain

import scala.collection.immutable.ListMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class JSONSchema(
  `type`:String,
  title:Option[String] = None,
  properties: Option[ListMap[String,JSONSchema]] = None,
  required: Option[Seq[String]] = None,
  readonly: Option[Boolean] = None,
  enum: Option[Seq[String]] = None,
  order: Option[Int] = None
)


object JSONSchema {
    
  def of(table:String,db:slick.driver.PostgresDriver.api.Database):Future[JSONSchema] = {
    
    val schema = new PgSchema(table,db)
    
    val map = schema.columns.map{ c => ListMap(properties(c): _*) }
    
    println(schema.columns)

    for{
      m <- map
      c <- schema.columns
    } yield {

      JSONSchema(
        `type` = "object",
        title = Some(table),
        properties = Some(m),
        readonly = Some(c.forall { x => x.is_updatable == "NO" }),
        required = Some(c.filter(_.is_nullable == "NO").map(_.column_name))
      )
    }
  }


  def keysOf(table:String,db:slick.driver.PostgresDriver.api.Database):Future[Seq[String]] = new PgSchema(table,db).pk
  
  
  def properties(columns:Seq[PgColumn]):Seq[(String,JSONSchema)] = {
    
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