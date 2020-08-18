package ch.wsl.box.codegen

import ch.wsl.box.jdbc.TypeMapping
import slick.model.Model



case class TableRegistryGenerator(entityList:Seq[String], model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


  def mapEntity(tableName:String):Option[String] = tables.find(_.model.name.table == tableName).map{ table =>
    s"""      case "${table.model.name.table}" => new TableRegistryEntry{
       |
       |         type MT = Entities.${table.TableClass.name}
       |
       |         override val name = "${table.model.name.table}"
       |         override val tableQuery = Entities.${table.TableValue.name}.asInstanceOf[TableQuery[Table[MT]]]
       |
       |
       |      }""".stripMargin
  }






  def generate(pkg:String, modelPackages:String):String =
    s"""package ${pkg}
       |
       |import ch.wsl.box.rest.runtime._
       |import ch.wsl.box.model.BoxTableRegistry
       |
       |object TableAccessRegistry extends TableRegistry {
       |
       |
       |  def table(name:String):TableRegistryEntry = {
       |
       |    import ch.wsl.box.jdbc.PostgresProfile.api._
       |
       |    name match {
       |${entityList.flatMap(mapEntity).mkString("\n")}
       |      case _ => BoxTableRegistry.table(name)
       |    }
       |
       |  }
       |
       |}

           """.stripMargin

  def writeToFile(folder:String, pkg:String, fileName:String, modelPackages:String) =
    writeStringToFile(generate(pkg, modelPackages),folder,pkg,fileName)




}