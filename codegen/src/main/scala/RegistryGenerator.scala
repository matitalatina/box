package ch.wsl.box.codegen

import slick.model.Model

case class RegistryGenerator(model:Model) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


  def generate(pkg:String,name:String,modelPackages:String):String =
    s"""package ${pkg}
       |
       |import ch.wsl.box.rest.runtime._
       |
       |class GenRegistry() extends RegistryInstance {
       |
       |    override val routes = GeneratedRoutes
       |    override val fileRoutes = FileRoutes
       |    override val actions = EntityActionsRegistry
       |    override val fields = FieldAccessRegistry
       |    override val tables = TableAccessRegistry
       |
       |}
           """.stripMargin

  override def writeToFile(folder:String, pkg:String, name:String, fileName:String,modelPackages:String) =
    writeStringToFile(generate(pkg,name,modelPackages),folder,pkg,fileName)

}