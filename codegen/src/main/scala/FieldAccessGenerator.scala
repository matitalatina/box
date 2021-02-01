package ch.wsl.box.codegen

import ch.wsl.box.jdbc.TypeMapping
import slick.model.Model



case class FieldAccessGenerator(tabs:Seq[String], views:Seq[String], model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


  def mapEntity(tableName:String):Option[String] = tables.find(_.model.name.table == tableName).map{ table =>
    s"""   case "${table.model.name.table}" => Map(
       |${mapField(table).mkString(",\n        ")}
       |   )""".stripMargin
  }

  def mapField(table:Table):Seq[String] = table.columns.map{ c =>
    val scalaType = TypeMapping(c.model).getOrElse(c.model.tpe)
    s"""      "${c.model.name}" -> ColType("${scalaType}",${c.model.nullable})"""
  }





  def generate(pkg:String, modelPackages:String):String =
    s"""package ${pkg}
       |
       |import ch.wsl.box.rest.runtime._
       |
       |object FieldAccessRegistry extends FieldRegistry {
       |
       |  override def tables: Seq[String] = Seq(
       |      ${tabs.mkString("\"","\",\n      \"","\"")}
       |  )
       |
       |  override def views: Seq[String] = Seq(
       |      ${views.mkString("\"","\",\n      \"","\"")}
       |  )
       |
       |
       |  def field(table:String,column:String):ColType = {
       |
       |    val tableFields:Map[String,ColType] = table match {
       |      ${(tabs++views).flatMap(mapEntity).mkString("\n      ")}
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