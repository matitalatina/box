package ch.wsl.box.codegen

import com.typesafe.config.Config
import slick.model.Model

case class EntityActionsRegistryGenerator(viewList:Seq[String], tableList:Seq[String], model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


    def mapTable(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""   case "${table.model.name.table}" => JsonTableActions[${table.TableClass.name},${table.EntityType.name}](${table.TableClass.name})"""
    }

    def mapView(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""   case "${table.model.name.table}" => JsonViewActions[${table.TableClass.name},${table.EntityType.name}](${table.TableClass.name})"""
    }


    def generate(pkg:String):String =
      s"""package ${pkg}
         |
         |import scala.concurrent.ExecutionContext
         |import ch.wsl.box.rest.logic.{JsonTableActions, JsonViewActions, EntityJsonTableActions, EntityJsonViewActions}
         |import Tables._
         |
         |class EntityActionsRegistry(implicit ec:ExecutionContext) {
         |
         |  import io.circe._
         |  import io.circe.generic.auto._
         |  import ch.wsl.box.rest.utils.JSONSupport._
         |
         |  def tableActions:String => EntityJsonTableActions = {
         |    ${tableList.flatMap(mapTable).mkString("\n")}
         |  }
         |
         |  def viewActions:String => EntityJsonViewActions = {
         |    ${viewList.flatMap(mapView).mkString("\n")}
         |  }
         |}
         |
         |object EntityActionsRegistry{
         |  def apply()(implicit ec: ExecutionContext) = new EntityActionsRegistry
         |}
           """.stripMargin

    def writeToFile(folder:String, pkg:String, fileName:String) =
      writeStringToFile(generate(pkg),folder,pkg,fileName)




}