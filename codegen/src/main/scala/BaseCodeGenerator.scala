package ch.wsl.box.codegen

import ch.wsl.box.jdbc.PostgresProfile
import com.typesafe.config.Config
import slick.jdbc.meta.MTable
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

trait BaseCodeGenerator {
  val dbConf: Config = com.typesafe.config.ConfigFactory.load().as[com.typesafe.config.Config]("db")
  private val dbPath = dbConf.as[String]("url")
  private val dbSchema = dbConf.as[String]("schema")

  private def db = PostgresProfile.api.Database.forURL(s"$dbPath?currentSchema=$dbSchema",
    driver="org.postgresql.Driver",
    user=dbConf.as[String]("user"),
    password=dbConf.as[String]("password"))

  private val tables:Seq[String] = dbConf.as[Seq[String]]("generator.tables")
  private val views:Seq[String] = dbConf.as[Seq[String]]("generator.views")

  private val excludes:Seq[String] = dbConf.as[Seq[String]]("generator.excludes")
  private val excludeFields:Seq[String] = dbConf.as[Seq[String]]("generator.excludeFields")

  private val tablesAndViews = tables ++ views

  val enabledTables = Await.result(db.run{
    MTable.getTables(None, None, None, Some(Seq("TABLE")))   //slick method to retrieve db structure
  }, 200 seconds)
    .filter { t =>
      if(excludes.exists(e => t.name.name matches e)) {
        false
      } else if(tables.contains("*")) {
        true
      } else {
        tables.contains(t.name.name)
      }
    }.distinct

  val enabledViews = Await.result(db.run{
    MTable.getTables(None, None, None, Some(Seq("VIEW")))
  }, 200 seconds)
    .filter { t =>
      if(excludes.exists(e => t.name.name matches e)) {
        false
      } else if(views.contains("*")) {
        true
      } else {
        views.contains(t.name.name)
      }
    }.distinct

  private val enabledEntities = enabledTables ++ enabledViews

  //println(enabledEntities.map(_.name.name))

  private val slickDbModel = Await.result(db.run{
    PostgresProfile.createModelBuilder(enabledEntities,true).buildModel   //create model based on specific db (here postgres)
  }, 300 seconds)


  //exclude fields
  private val cleanedEntities = slickDbModel.tables.filter{t =>
    dbSchema match {
      case "public" => t.name.schema.isEmpty
      case _ => t.name.schema == Some(dbSchema)
    }
  }.map{ table =>

    table.copy(columns = table.columns.filterNot{c =>
      excludeFields.exists(e => c.name matches e )
    })
  }

  val dbModel = slickDbModel.copy(tables = cleanedEntities)
}
