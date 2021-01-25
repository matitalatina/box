package ch.wsl.box.codegen

import slick.codegen.SourceCodeGenerator


/**
 *  This customizes the Slick code generator.
 *  For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
 */

case class GeneratedFiles(
                       entities: SourceCodeGenerator,
                       generatedRoutes: RoutesGenerator,
                       entityActionsRegistry: EntityActionsRegistryGenerator,
                       fileAccessGenerator: FileAccessGenerator,
                       registry: RegistryGenerator,
                       fieldRegistry: FieldAccessGenerator
                         )

object CustomizedCodeGenerator extends BaseCodeGenerator {

  def generatedFiles():GeneratedFiles = {


    val calculatedViews = enabledViews.map(_.name.name).distinct
    val calculatedTables = enabledTables.map(_.name.name).distinct

    GeneratedFiles(
      entities = EntitiesGenerator(dbModel,dbConf),
      generatedRoutes = RoutesGenerator(calculatedViews,calculatedTables,dbModel),
      entityActionsRegistry = EntityActionsRegistryGenerator(calculatedViews ++ calculatedTables,dbModel),
      fileAccessGenerator = FileAccessGenerator(dbModel,dbConf),
      registry = RegistryGenerator(dbModel),
      fieldRegistry = FieldAccessGenerator(calculatedTables ++ calculatedViews,dbModel)
    )

  }

  def main(args: Array[String]):Unit = {

    val files = generatedFiles()

    files.entities.writeToFile(
      "ch.wsl.box.jdbc.PostgresProfile",
      args(0),
      "ch.wsl.box.generated",
      "Entities",
      "Entities.scala"
    )


    files.generatedRoutes.writeToFile(
      args(0),
      "ch.wsl.box.generated",
      "GeneratedRoutes",
      "GeneratedRoutes.scala",
      "Entities"
    )

    files.entityActionsRegistry.writeToFile(
      args(0),
      "ch.wsl.box.generated",
      "EntityActionsRegistry.scala",
      "Entities"
    )

    files.fileAccessGenerator.writeToFile(
      args(0),
      "ch.wsl.box.generated",
      "FileRoutes",
      "FileRoutes.scala",
      "Entities"
    )

    files.fieldRegistry.writeToFile(args(0),"ch.wsl.box.generated","GenFieldRegistry.scala","")


    files.registry.writeToFile(args(0),"ch.wsl.box.generated","","GenRegistry.scala")




  }




}

