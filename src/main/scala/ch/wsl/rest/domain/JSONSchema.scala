package ch.wsl.rest.domain

import ch.wsl.rest.service.Auth

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

  import StringHelper._

  def of(table:String,db:slick.driver.PostgresDriver.api.Database):Future[JSONSchema] = {

    println("Getting JSONSchema of:" + table)

    val schema = new PgSchema(table,db)
    
    val map = schema.columns.map{ c => ListMap(properties(c): _*) }

    println("columns")

    for{
      m <- map
      c <- schema.columns
    } yield {
      JSONSchema(
        `type` = "object",
        title = Some(table),
        properties = Some(m),
        readonly = Some(c.forall { x => x.is_updatable == "NO" }),
        required = Some(c.filter(_.is_nullable == "NO").map(_.column_name.slickfy))
      )
    }
  }



  def keysOf(table:String):Future[Seq[String]] = {
    new PgSchema(table,Auth.adminDB).pk.map { pks =>
      pks.map(_.slickfy)
    }

  }
  
  
  def properties(columns:Seq[PgColumn]):Seq[(String,JSONSchema)] = {
    
    val cols = {for{
      c <- columns
    } yield {
      c.column_name.slickfy -> JSONSchema(typesMapping(c.data_type),Some(c.column_name.slickfy),order=Some(c.ordinal_position),readonly=Some(c.is_updatable == "NO"))
    }}.toList
    
    
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