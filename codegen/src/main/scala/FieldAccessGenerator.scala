package ch.wsl.box.codegen

import slick.model.Model



case class FieldAccessGenerator(entityList:Seq[String], model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


  def mapEntity(tableName:String):Option[String] = tables.find(_.model.name.table == tableName).map{ table =>
    s"""   case "${table.model.name.table}" => Map(
       |${mapField(table).mkString(",\n        ")}
       |   )""".stripMargin
  }

  def mapField(table:Table):Seq[String] = table.columns.map{ c =>
    s"""      "${c.model.name}" -> ColType("${c.model.tpe}",${c.model.nullable})"""
  }




  def generate(pkg:String, modelPackages:String):String =
    s"""package ${pkg}
       |
       |import ch.wsl.box.rest.runtime._
       |
       |object FieldAccessRegistry extends FieldRegistry {
       |
       |
       |  def field(table:String,column:String):ColType = {
       |
       |    val tableFields:Map[String,ColType] = table match {
       |      ${entityList.flatMap(mapEntity).mkString("\n      ")}
       |      case _ => Map()
       |    }
       |
       |    tableFields.get(column).getOrElse(ColType("Unknown", false))
       |
       |  }
       |
       |}

           """.stripMargin

  def writeToFile(folder:String, pkg:String, fileName:String, modelPackages:String) =
    writeStringToFile(generate(pkg, modelPackages),folder,pkg,fileName)




}