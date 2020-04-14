package ch.wsl.box.rest.runtime

import ch.wsl.box.codegen.CustomizedCodeGenerator
import scribe.{Logging}

import scala.reflect.runtime.currentMirror

trait RegistryInstance{
  def fileRoutes:GeneratedFileRoutes
  def routes: GeneratedRoutes
  def actions: ActionRegistry
}

case class GeneratedRegistry(
                   fileRoutes:GeneratedFileRoutes,
                   routes: GeneratedRoutes,
                   actions: ActionRegistry
                   ) extends RegistryInstance

object Registry extends Logging {

  private def dropPackage(code:String):String = code.lines.drop(1).mkString("\n")

  private var _registry:RegistryInstance = null;


  def apply():RegistryInstance = _registry

  def load() = {
  try {
    _registry = Class.forName("ch.wsl.box.generated.GenRegistry")
                      .newInstance()
                      .asInstanceOf[RegistryInstance]
    logger.warn("Using generated registry, use only in development!")
  } catch { case t:Throwable =>

      val files = CustomizedCodeGenerator.generatedFiles()

      val entitiesCode =  files.entities.packageCode(
        profile = "ch.wsl.box.jdbc.PostgresProfile",
        pkg = "",
        container = "Entities",
        parentType = None
      )

      val fileEntitiesCode =  files.fileTables.packageCode(
        profile = "ch.wsl.box.jdbc.PostgresProfile",
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

}
