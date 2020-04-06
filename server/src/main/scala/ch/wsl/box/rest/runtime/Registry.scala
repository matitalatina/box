package ch.wsl.box.rest.runtime

import ch.wsl.box.codegen.CustomizedCodeGenerator

import scala.reflect.runtime.currentMirror

case class GeneratedRegistry(
                   fileRoutes:GeneratedFileRoutes,
                   routes: GeneratedRoutes,
                   actions: ActionRegistry
                   )

object Registry {

  private def dropPackage(code:String):String = code.lines.drop(1).mkString("\n")

  private var _registry:GeneratedRegistry = null;


  def apply():GeneratedRegistry = _registry

  def load() = {
    val files = CustomizedCodeGenerator.generatedFiles()

    val entitiesCode =  files.entities.packageCode(
      profile = "ch.wsl.box.rest.jdbc.PostgresProfile",
      pkg = "",
      container = "Entities",
      parentType = None
    )

    val fileEntitiesCode =  files.fileTables.packageCode(
      profile = "ch.wsl.box.rest.jdbc.PostgresProfile",
      pkg = "",
      container = "FileTables",
      parentType = None
    )

    val routeFileCode =  files.fileAccessGenerator.generate(
      pkg = "",
      modelPackages = "FileTables",
      name = "FileRoutes"
    )

    val routesCode =  files.generatedRoutes.generate(
      pkg = "",
      modelPackages = "Entities",
      name = "GenRoutes"
    )

    val actionsCode =  files.entityActionsRegistry.generate(
      pkg = "",
      modelPackages = "Entities"
    )

    val source = s"""
                    |${dropPackage(entitiesCode)}
                    |
                    |${dropPackage(fileEntitiesCode)}
                    |
                    |${dropPackage(routeFileCode)}
                    |
                    |${dropPackage(routesCode)}
                    |
                    |${dropPackage(actionsCode)}
                    |
                    |ch.wsl.box.rest.runtime.GeneratedRegistry(
                    | routes = GenRoutes,
                    | fileRoutes = FileRoutes,
                    | actions = EntityActionsRegistry
                    |)
                    |""".stripMargin


    //uncomment for debugging
    //reflect.io.File("test.out.scala").writeAll(source)
    //println(new java.io.File(".").getAbsolutePath())

    import scala.tools.reflect.ToolBox
    try {
      val toolbox = currentMirror.mkToolBox()
      val tree = toolbox.parse(source)
      _registry = toolbox.eval(tree).asInstanceOf[GeneratedRegistry]
    } catch { case e:Throwable =>
      e.printStackTrace()
      reflect.io.File("ErroredRuntimeCompile.scala").writeAll(source)
      println(s"Saved generated file in: ${new java.io.File(".").getAbsolutePath()}/ErroredRuntimeCompile.scala")
    }
  }

}
