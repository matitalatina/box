package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.{JSONFieldMap, JSONFieldHTTPOption, JSONFieldOptions, JSONField}
import com.typesafe.config._
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object JSONForm {

  import StringHelper._
  
  def tableFieldTitles: Config = {
    val config = ConfigFactory.load()
    println("CONFIG")
    println(config)

    config.as[Config]("rest.lookup.labels")
  }

  def of(table:String,db:slick.driver.PostgresDriver.api.Database):Future[Seq[JSONField]] = {

    val schema = new PgInformationSchema(table, db)

    println(schema.fk)

    var constraints = List[String]()

    def field2form(field: PgColumn): Future[JSONField] = schema.findFk(field.column_name).map {
      _ match {
        case Some(fk) => {
          if (constraints.contains(fk.contraintName)) {
            println("error: " + fk.contraintName)
            println(field.column_name)
            JSONField(JSONSchemas.typesMapping(field.data_type),table = table, key = field.column_name.slickfy)
          } else {
            constraints = fk.contraintName :: constraints

            val title = tableFieldTitles.as[Option[String]](fk.referencingTable).getOrElse("en")

            JSONField(
              JSONSchemas.typesMapping(field.data_type),
              key = field.column_name.slickfy,
              table = table,
              placeholder = Some(fk.referencingTable + " Lookup"),
              options = Some(
                JSONFieldOptions(JSONFieldHTTPOption("http://localhost:8080/" + fk.referencingTable), JSONFieldMap(fk.referencingKeys.head, title))
              )
            )
          }
        }
        case _ => JSONField(JSONSchemas.typesMapping(field.data_type),table = table, key = field.column_name.slickfy, widget = Some("myinputwidget"))
      }
    }

    schema.columns.flatMap{ c => Future.sequence(c.map(field2form))}


  }
  
  
  
  
  
  
  
}