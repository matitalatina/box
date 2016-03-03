
package ch.wsl.codegen

import net.ceedubs.ficus.Ficus._

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

    val tablesAndViews = tables ++ views

    val enabledModels = Await.result(db.run{
      MTable.getTables(None, None, None, Some(Seq("TABLE", "VIEW")))
    }, 200 seconds)
      .filter(t => tablesAndViews.contains(t.name.name))

    val model = Await.result(db.run{
      PostgresDriver.createModelBuilder(enabledModels,true).buildModel
    }, 200 seconds)





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

          ${indent(code)}
}
      """.trim()
      }
    }

    val codegen = new slick.codegen.SourceCodeGenerator(model) with MyOutputHelper {




      // override table generator
      override def Table = new Table(_){
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

              if(model.columns.size < 22) {
                result
              } else {
                result + s"""
    object ${TableClass.elementType}{
      def factoryHList(hlist:HList) = {
        val x = hlist.toList
        ${TableClass.elementType}("""+columns.zipWithIndex.map(x => "x("+x._2+").asInstanceOf["+x._1.actualType+"]").mkString(",")+s""");
      }

      def toHList(e:${TableClass.elementType}) = {
        Option(( """+columns.map(c => "e."+ c.name + " :: ").mkString("")+s""" HNil))
      }
    }
                 """
              }

          }
        }



        override def factory = if(model.columns.size < 22 ) {
          super.factory
        } else {

          s"${TableClass.elementType}.factoryHList"

        }

        override def extractor = if(model.columns.size < 22 ) {
          super.extractor
        } else {
          s"${TableClass.elementType}.toHList"
        }

        override def mappingEnabled = true;

        override def TableClass = new TableClassDef {
          override def optionEnabled = columns.size < 22 && mappingEnabled && columns.exists(c => !c.model.nullable)
        }


      }

    }


    println("codegen created")
    println(codegen)

    codegen.writeToFile(
      "slick.driver.PostgresDriver",
      args(0),
      "ch.wsl.model",
      "Tables",
      "Tables.scala"
    )


      println("Exit")


  }



}

