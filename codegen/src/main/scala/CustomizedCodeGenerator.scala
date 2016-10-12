package ch.wsl.box.codegen

import net.ceedubs.ficus.Ficus._
import slick.codegen.OutputHelpers

import slick.jdbc.meta.MTable
import slick.model.Model
import slick.driver.PostgresDriver


import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *  This customizes the Slick code generator. We only do simple name mappings.
 *  For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
 */
object CustomizedCodeGenerator {
  def main(args: Array[String]):Unit = {


    println("Generating " + args(0))


    val dbConf = com.typesafe.config.ConfigFactory.load().as[com.typesafe.config.Config]("db")

    def db = PostgresDriver.api.Database.forURL(dbConf.as[String]("url"),
      driver="org.postgresql.Driver",
      user=dbConf.as[String]("user"),
      password=dbConf.as[String]("password"))

    val tables:Seq[String] = dbConf.as[Seq[String]]("generator.tables")
    val views:Seq[String] = dbConf.as[Seq[String]]("generator.views")

    val excludes:Seq[String] = dbConf.as[Seq[String]]("generator.excludes")

    val tablesAndViews = tables ++ views

    val enabledTables = Await.result(db.run{
      MTable.getTables(None, None, None, Some(Seq("TABLE")))
    }, 200 seconds)
      .filter { t =>
        if(excludes.exists(e => t.name.name == e)) {
          false
        } else if(tables.contains("*")) {
          true
        } else {
          tables.contains(t.name.name)
        }
      }

    val enabledViews = Await.result(db.run{
      MTable.getTables(None, None, None, Some(Seq("VIEW")))
    }, 200 seconds)
      .filter { t =>
        if(excludes.exists(e => t.name.name == e)) {
          false
        } else if(views.contains("*")) {
          true
        } else {
          views.contains(t.name.name)
        }
      }

    val enabledModels = enabledTables ++ enabledViews

    //println(enabledModels.map(_.name.name))

    val dbModel = Await.result(db.run{
      PostgresDriver.createModelBuilder(enabledModels,true).buildModel
    }, 200 seconds)


    val allColumns = dbModel.tables.flatMap(_.columns.map(_.name))

    dbModel.tables.foreach{ t =>
      val dup = t.columns.map(_.name).diff(t.columns.map(_.name).distinct).distinct
      if(dup.size > 0) {
        println(t.name.table)
        println(dup)
        println("")
        println("")
        println(t.columns.filter(_.name == dup.head))
        println("")
        println("")
      }
    }


    val gen = codegen(dbModel)

//    println("codegen created")
//    println(gen)

    gen.writeToFile(
      "slick.driver.PostgresDriver",
      args(0),
      "ch.wsl.box.model",
      "Tables",
      "Tables.scala"
    )




    val calculatedViews = enabledModels.filter(_.tableType == "VIEW").map(_.name.name)
    val calculatedTables= enabledModels.filter(_.tableType == "TABLE").map(_.name.name)
    val routeGen = gen.RoutesGenerator(calculatedViews,calculatedTables)

    routeGen.writeToFile(
      args(0),
      "ch.wsl.box.rest.service",
      "GeneratedRoutes",
      "GeneratedRoutes.scala",
      "ch.wsl.box.model.tables"
    )


    println("Exit")


  }





    trait MyOutputHelper extends slick.codegen.OutputHelpers {
      override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) : String= {
        s"""
package ${pkg}
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */

  import slick.driver.PostgresDriver.api._
  import slick.model.ForeignKeyAction
  import slick.collection.heterogeneous._
  import slick.collection.heterogeneous.syntax._

package object tables {


      val profile = slick.driver.PostgresDriver

      import profile._

          ${indent(code)}
}
      """.trim()
      }
    }

    def codegen(model:Model) = new slick.codegen.SourceCodeGenerator(model) with MyOutputHelper {

      tables.head.EntityType.name

      case class RoutesGenerator(viewList:Seq[String],tableList:Seq[String]) {
        def singleRoute(method:String,model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
          s"""$method[${table.TableClass.name},${table.EntityType.name}]("${table.model.name.table}",${table.TableClass.name})"""
        }

        def composeRoutes():String = {
          (
            tableList.flatMap(t => singleRoute("model",t)) ++
            viewList.flatMap(v => singleRoute("view",v))
          ).mkString(" ~ \n")
        }

        def generate(pkg:String,name:String,modelPackages:String):String =
          s"""package ${pkg}
             |
             |import akka.http.scaladsl.server.{Directives, Route}
             |import akka.stream.Materializer
             |import $modelPackages._
             |import de.heikoseeberger.akkahttpcirce.CirceSupport
             |
             |trait $name extends RouteTable with RouteView {
             |  import CirceSupport._
             |  import Directives._
             |  import ch.wsl.box.rest.model.JsonImplicits._
             |  import io.circe.generic.auto._
             |
             |  def generatedRoutes()(implicit db:slick.driver.PostgresDriver.api.Database, mat:Materializer):Route = {
             |    ${composeRoutes()}
             |  }
             |}
           """.stripMargin

          def writeToFile(folder:String, pkg:String, name:String, fileName:String,modelPackages:String) =
            writeStringToFile(generate(pkg,name,modelPackages),folder,pkg,fileName)

      }


      override def tableName = (dbName: String) => toCamelCase(dbName)
      /** Maps database table name to entity case class name
 *
        *@group Basic customization overrides */
      override def entityName = (dbName: String) => toCamelCase(dbName)+"Row"

      /**
        * Capitalizes the first (16 bit) character of each word separated by one or more '_'. Lower cases all other characters.
        * Removes one '_' from each sequence of one or more subsequent '_' (to avoid collision).
        * (Warning: Not unicode-safe, uses String#apply)
        */
      def toCamelCase(str:String): String = {

        val result = str.toLowerCase
          .split("_")
          .map{ case "" => "_" case s => s } // avoid possible collisions caused by multiple '_'
          .map(_.capitalize)
          .mkString("")

        val resultWithUnderscore = if(str.endsWith("_")) {
          result + "_"
        } else {
          result
        }


        resultWithUnderscore

      }

      // override table generator
      override def Table = new Table(_){

        //disable plain override mapping, with hlist produces compile time problem with non-primitives types
        override def PlainSqlMapper = new PlainSqlMapperDef {
          override def code = ""
        }

        // disable entity class generation and mapping
        override def EntityType = new EntityType{
          override def code = {
            val args = columns.map(c=>
              c.default.map( v =>
                s"${c.name}: ${c.exposedType} = $v"
              ).getOrElse(
                  s"${c.name}: ${c.exposedType}"
                )
            ).mkString(", ")

              val prns = (parents.take(1).map(" extends "+_) ++ parents.drop(1).map(" with "+_)).mkString("")
              val result = s"""case class $name($args)$prns"""

              if(model.columns.size <= 22) {
                result
              } else {
                result + s"""
    object ${TableClass.elementType}{

      type ${TableClass.elementType}HList = ${columns.map(_.exposedType).mkString(" :: ")} :: HNil

      def factoryHList(hlist:${TableClass.elementType}HList):${TableClass.elementType} = {
        val x = hlist.toList
        ${TableClass.elementType}("""+columns.zipWithIndex.map(x => "x("+x._2+").asInstanceOf["+x._1.actualType+"]").mkString(",")+s""");
      }

      def toHList(e:${TableClass.elementType}):Option[${TableClass.elementType}HList] = {
        Option(( """+columns.map(c => "e."+ c.name + " :: ").mkString("")+s""" HNil))
      }
    }
                 """
              }

          }
        }



        override def factory = if(model.columns.size <= 22 ) {
          super.factory
        } else {

          s"${TableClass.elementType}.factoryHList"

        }

        override def extractor = if(model.columns.size <= 22 ) {
          super.extractor
        } else {
          s"${TableClass.elementType}.toHList"
        }

        override def mappingEnabled = true;

        override def TableClass = new TableClassDef {
          override def optionEnabled = columns.size <= 22 && mappingEnabled && columns.exists(c => !c.model.nullable)
        }


      }

    }








}

