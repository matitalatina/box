package ch.wsl.box.codegen

import com.typesafe.config.Config
import slick.model.Model

case class EntityActionsRegistryGenerator(viewList:Seq[String], tableList:Seq[String], model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


    def mapTable(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""   "${table.model.name.table}" -> JsonTableActions[${table.TableClass.name},${table.EntityType.name}](${table.TableClass.name})"""
    }

    def mapView(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""   "${table.model.name.table}" -> JsonViewActions[${table.TableClass.name},${table.EntityType.name}](${table.TableClass.name})"""
    }

//    def mapModelFile(model:String):Option[String] = tables.find(t => t.model.name.table == model && t.model.columns.exists(_.tpe == "Array[Byte]")).map{ table =>
//      s"""   "${table.model.name.table}" -> JsonActions[FileTables.${table.TableClass.name},FileTables.${table.EntityType.name}](FileTables.${table.TableClass.name})"""
//    }

    def implicits(model:String):Option[String] = tables.find(_.model.name.table == model).map{ table =>
      s"""implicit val ${table.model.name.table}_decoder: Decoder[${table.EntityType.name}] = deriveDecoder[${table.EntityType.name}]
         |implicit val ${table.model.name.table}_encoder: Encoder[${table.EntityType.name}] = deriveEncoder[${table.EntityType.name}]
           """.stripMargin
    }

    def generate(pkg:String):String =
      s"""package ${pkg}
         |
         |import ch.wsl.box.rest.logic.{JsonTableActions, JsonViewActions, EntityJsonTableActions, EntityJsonViewActions}
         |import Tables._
         |
         |object EntityActionsRegistry {
         |
         |  import io.circe._
         |  import io.circe.generic.auto._
         |  import ch.wsl.box.rest.utils.JSONSupport._
         |
         |  val tableActions: Map[String, EntityJsonTableActions] = Map(
         |    ${tableList.flatMap(mapTable).mkString(",\n")}
         |    )
         |
         |  val viewActions: Map[String, EntityJsonViewActions] = Map(
         |    ${viewList.flatMap(mapView).mkString(",\n")}
         |    )
         |  }
           """.stripMargin

    def writeToFile(folder:String, pkg:String, fileName:String) =
      writeStringToFile(generate(pkg),folder,pkg,fileName)




}