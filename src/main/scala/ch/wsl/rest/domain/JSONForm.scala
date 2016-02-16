package ch.wsl.rest.domain

import com.typesafe.config._
import net.ceedubs.ficus.Ficus._


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class TitleMap(value:String,name:String)

case class JSONField(
  `type`:String,
  key:Option[String] = None,
  title:Option[String] = None,
  titleMap:Option[List[TitleMap]] = None,
  options:Option[JSONFieldOptions] = None,
  placeholder:Option[String] = None
)

case class JSONFieldOptions(async:JSONFieldHTTPOption, map:JSONFieldMap)

case class JSONFieldMap(valueProperty:String,textProperty:String)

case class JSONFieldHTTPOption(url:String)



object JSONForm {

  import StringHelper._
  
  val tableFieldTitles: Config = ConfigFactory.load().as[Config]("rest.lookup.labels")
  
  def of(table:String,db:slick.driver.PostgresDriver.api.Database):Future[Seq[JSONField]] = {

    val schema = new PgSchema(table, db)

    println(schema.fk)

    var constraints = List[String]()

    def field2form(field: PgColumn): Future[Option[JSONField]] = schema.findFk(field.column_name).map {
      _ match {
        case Some(fk) => {
          if (constraints.contains(fk.contraintName)) {
            None
          } else {
            constraints = fk.contraintName :: constraints

            val title = tableFieldTitles.as[Option[String]](fk.referencingTable).getOrElse("en")

            Some(JSONField(
              JSONSchema.typesMapping(field.data_type),
              key = Some(field.column_name),
              placeholder = Some(fk.referencingTable + " Lookup"),
              options = Some(
                JSONFieldOptions(JSONFieldHTTPOption("http://localhost:8080/" + fk.referencingTable), JSONFieldMap(fk.referencingKeys.head, title))
              )
            ))
          }
        }
        case _ => Some(JSONField(JSONSchema.typesMapping(field.data_type), key = Some(field.column_name.slickfy)))
      }
    }

    schema.columns.flatMap{ c => Future.sequence(c.map(field2form))}.map(_.flatten)


  }
  
  
  
  
  
  
  
}