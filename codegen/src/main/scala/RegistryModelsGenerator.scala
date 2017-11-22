package ch.wsl.box.codegen

import com.typesafe.config.Config
import slick.model.Model

case class RegistryModelsGenerator(viewList:Seq[String],tableList:Seq[String],model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


    def mapModel(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""   "${table.model.name.table}" -> JsonActions[${table.TableClass.name},${table.EntityType.name}](${table.TableClass.name})"""
    }

    def implicits(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""implicit val ${table.model.name.table}_decoder: Decoder[${table.EntityType.name}] = deriveDecoder[${table.EntityType.name}]
         |implicit val ${table.model.name.table}_encoder: Encoder[${table.EntityType.name}] = deriveEncoder[${table.EntityType.name}]
           """.stripMargin
    }

    def generate(pkg:String):String =
      s"""package ${pkg}
         |
             |import ch.wsl.box.rest.logic.{JsonActions, ModelJsonActions}
         |import tables._
         |
             |object TablesRegistry {
         |
             |  import io.circe._
         |  import io.circe.generic.auto._
         |  import ch.wsl.box.rest.utils.JSONSupport._
         |
             |
             |  val actions: Map[String, ModelJsonActions] = Map(
         |  ${tableList.flatMap(mapModel).mkString(",\n")}
         |  )
         |
             |}
           """.stripMargin

    def writeToFile(folder:String, pkg:String, fileName:String) =
      writeStringToFile(generate(pkg),folder,pkg,fileName)




}