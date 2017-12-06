package ch.wsl.box.rest.logic

import ch.wsl.box.model.TablesRegistry
import ch.wsl.box.model.shared._
import com.typesafe.config._
import net.ceedubs.ficus.Ficus._
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object JSONEntityMetadata {

  import StringHelper._
  
  def tableFieldTitles: Config = {
    val config = ConfigFactory.load()

    config.as[Config]("rest.lookup.labels")
  }

  def defaultTableLookupField:String = {
    tableFieldTitles.as[Option[String]]("default").getOrElse("name")
  }

  def of(table:String,lang:String)(implicit db:Database):Future[JSONMetadata] = {

    val schema = new PgInformationSchema(table, db)

    println(schema.fk)

    var constraints = List[String]()

    def field2form(field: PgColumn): Future[JSONField] = schema.findFk(field.column_name).flatMap {
      _ match {
        case Some(fk) => {
          if (constraints.contains(fk.constraintName)) {
            println("error: " + fk.constraintName)
            println(field.column_name)
            Future.successful(JSONField(field.jsonType, key = field.boxName, nullable = field.nullable))
          } else {
            constraints = fk.constraintName :: constraints

            val text = tableFieldTitles.as[Option[String]](fk.referencingTable).getOrElse(defaultTableLookupField)
            val model = fk.referencingTable
            val value = fk.referencingKeys.head

            import ch.wsl.box.shared.utils.JsonUtils._
            TablesRegistry.actions(model).getEntity(JSONQuery.baseQuery).map{ lookupData =>
              val options = lookupData.map{ lookupRow =>
                (lookupRow.get(value),lookupRow.get(text))
              }.toMap

              JSONField(
                field.jsonType,
                key = field.boxName,
                nullable = field.nullable,
                placeholder = Some(fk.referencingTable + " Lookup"),
                //widget = Some(WidgetsNames.select),
                lookup = Some(JSONFieldOptions(model, JSONFieldMap(value,text),options))
              )
            }

          }
        }
        case _ => Future.successful(JSONField(
          field.jsonType,
          key = field.boxName,
          nullable = field.nullable,
          widget = JSONSchemas.defaultWidgetMapping(field.data_type)
        ))
      }
    }



    for{
      c <- schema.columns
      fields <- Future.sequence(c.map(field2form))
      keys <- JSONSchemas.keysOf(table)
    } yield {
      JSONMetadata(1,table,fields, Layout.fromFields(fields),table,lang,fields.map(_.key),keys, None)
    }


  }

}