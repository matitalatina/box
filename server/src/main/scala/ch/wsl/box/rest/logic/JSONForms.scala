package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared._
import com.typesafe.config._
import net.ceedubs.ficus.Ficus._


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object JSONForms {

  import StringHelper._
  
  def tableFieldTitles: Config = {
    val config = ConfigFactory.load()

    config.as[Config]("rest.lookup.labels")
  }

  def defaultTableLookupField:String = {
    tableFieldTitles.as[Option[String]]("default").getOrElse("name")
  }

  def of(table:String,db:slick.driver.PostgresDriver.api.Database,lang:String):Future[JSONForm] = {

    val schema = new PgInformationSchema(table, db)

    println(schema.fk)

    var constraints = List[String]()

    def field2form(field: PgColumn): Future[JSONField] = schema.findFk(field.column_name).map {
      _ match {
        case Some(fk) => {
          if (constraints.contains(fk.contraintName)) {
            println("error: " + fk.contraintName)
            println(field.column_name)
            JSONField(JSONSchemas.typesMapping(field.data_type), key = field.column_name.slickfy)
          } else {
            constraints = fk.contraintName :: constraints

            val title = tableFieldTitles.as[Option[String]](fk.referencingTable).getOrElse(defaultTableLookupField)

            JSONField(
              JSONSchemas.typesMapping(field.data_type),
              key = field.column_name.slickfy,
              placeholder = Some(fk.referencingTable + " Lookup"),
              //widget = Some(WidgetsNames.select),
              options = Some(
                JSONFieldOptions(fk.referencingTable, JSONFieldMap(fk.referencingKeys.head, title))
              )
            )
          }
        }
        case _ => JSONField(
          JSONSchemas.typesMapping(field.data_type),
          key = field.column_name.slickfy,
          widget = JSONSchemas.widgetMapping(field.data_type)
        )
      }
    }

    schema.columns.flatMap{ c => Future.sequence(c.map(field2form))}.map{ fields =>
      JSONForm(1,fields,Layout.fromFields(fields),table,lang,fields.map(_.key))
    }


  }
  
  
  
  
  
  
  
}