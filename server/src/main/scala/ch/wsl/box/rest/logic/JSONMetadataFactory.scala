package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.model.TablesRegistry
import ch.wsl.box.rest.utils.Auth
import com.typesafe.config._
import net.ceedubs.ficus.Ficus._
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}


object JSONMetadataFactory {

  import StringHelper._
  
  def tableFieldTitles: Config = {
    val config = ConfigFactory.load()

    config.as[Config]("rest.lookup.labels")
  }

  def defaultTableLookupField:String = {
    tableFieldTitles.as[Option[String]]("default").getOrElse("name")
  }

  def of(table:String,lang:String)(implicit db:Database, mat:Materializer, ec:ExecutionContext):Future[JSONMetadata] = {

    val schema = new PgInformationSchema(table, db)

//    println(schema.fk)

    var constraints = List[String]()

    def field2form(field: PgColumn): Future[JSONField] = schema.findFk(field.column_name).flatMap {
      _ match {
        case Some(fk) => {
          if (constraints.contains(fk.constraintName)) {
            println("error: " + fk.constraintName)
            println(field.column_name)
            Future.successful(JSONField(field.jsonType, name = field.boxName, nullable = field.nullable))
          } else {
            constraints = fk.constraintName :: constraints

            val text = tableFieldTitles.as[Option[String]](fk.referencingTable).getOrElse(defaultTableLookupField)
            val model = fk.referencingTable
            val value = fk.referencingKeys.head

            import ch.wsl.box.shared.utils.JsonUtils._
            TablesRegistry().tableActions(model).getEntity().map{ lookupData =>
              val options = lookupData.map{ lookupRow =>
                (lookupRow.get(value),lookupRow.get(text))
              }.toMap

              JSONField(
                field.jsonType,
                name = field.boxName,
                nullable = field.nullable,
                placeholder = Some(fk.referencingTable + " Lookup"),
                //widget = Some(WidgetsNames.select),
                lookup = Some(JSONFieldLookup(model, JSONFieldMap(value,text),options))
              )
            }

          }
        }
        case _ => Future.successful(JSONField(
          field.jsonType,
          name = field.boxName,
          nullable = field.nullable,
          widget = JSONMetadataFactory.defaultWidgetMapping(field.data_type)
        ))
      }
    }



    for{
      c <- schema.columns
      fields <- Future.sequence(c.map(field2form))
      keys <- JSONMetadataFactory.keysOf(table)
    } yield {
      JSONMetadata(1,table,table,fields, Layout.fromFields(fields),table,lang,fields.map(_.name),keys, None)
    }


  }
  def keysOf(table:String)(implicit ec:ExecutionContext):Future[Seq[String]] = {
    println("Getting " + table + " keys")
    new PgInformationSchema(table,Auth.adminDB).pk.map { pk =>   //map to enter the future
      println(pk)
      pk.boxKeys
    }

  }

  def isView(table:String)(implicit ec:ExecutionContext):Future[Boolean] =
    new PgInformationSchema(table,Auth.adminDB).pgTable.map(_.isView)  //map to enter the future


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