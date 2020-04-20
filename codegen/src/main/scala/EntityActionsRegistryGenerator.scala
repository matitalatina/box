package ch.wsl.box.codegen

import com.typesafe.config.Config
import slick.model.Model

case class EntityActionsRegistryGenerator(viewList:Seq[String], tableList:Seq[String], model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


    def mapTable(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""   case "${table.model.name.table}" => JSONTableActions[${table.TableClass.name},${table.EntityType.name}](${table.TableClass.name})"""
    }

    def mapView(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""   case "${table.model.name.table}" => Some(JSONViewActions[${table.TableClass.name},${table.EntityType.name}](${table.TableClass.name}))"""
    }


    def generate(pkg:String, modelPackages:String):String =
      s"""package ${pkg}
         |
         |import scala.concurrent.ExecutionContext
         |import scala.util.Try
         |import ch.wsl.box.rest.logic.{JSONTableActions, JSONViewActions, TableActions, ViewActions}
         |
         |import ch.wsl.box.rest.runtime._
         |
         |object EntityActionsRegistry extends ActionRegistry {
         |
         |  import $modelPackages._
         |  import io.circe._
         |  import io.circe.generic.auto._
         |  import ch.wsl.box.rest.utils.JSONSupport._
         |
         |  def tableActions(implicit ec: ExecutionContext) :String => TableActions[Json] = {
         |    ${tableList.flatMap(mapTable).mkString("\n")}
         |  }
         |
         |  def viewActions(implicit ec: ExecutionContext) :String => Option[ViewActions[Json]] = {
         |    ${viewList.flatMap(mapView).mkString("\n")}
         |    case _ => None
         |  }
         |
         |  def actions(name:String)(implicit ec: ExecutionContext) : Option[ViewActions[Json]] = viewActions(ec)(name).orElse(Try(tableActions(ec)(name)).toOption)
         |
         |}

           """.stripMargin

    def writeToFile(folder:String, pkg:String, fileName:String, modelPackages:String) =
      writeStringToFile(generate(pkg, modelPackages),folder,pkg,fileName)




}
