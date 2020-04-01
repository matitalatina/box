package ch.wsl.box.codegen

import slick.codegen.SourceCodeGenerator


/**
 *  This customizes the Slick code generator.
 *  For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
 */

case class GeneratedFiles(
                       entities: SourceCodeGenerator,
                       fileTables: SourceCodeGenerator,
                       generatedRoutes: RoutesGenerator,
                       entityActionsRegistry: EntityActionsRegistryGenerator,
                       fileAccessGenerator: FileAccessGenerator
                         ) {

}

object CustomizedCodeGenerator extends BaseCodeGenerator {

  def generatedFiles():GeneratedFiles = {

    val modelWithoutFiles = dbModel.copy(tables = dbModel.tables.map { table =>
      table.copy(columns = table.columns.filterNot { c =>
        c.tpe == "Array[Byte]"
      })
    })


    val modelWithOnlyFilesTables = dbModel.copy(tables = dbModel.tables.filter(_.columns.exists(_.tpe == "Array[Byte]")).map{ t =>
      t.copy(foreignKeys = Seq())
    })

    val calculatedViews = enabledViews.map(_.name.name).distinct
    val calculatedTables= enabledTables.map(_.name.name).distinct

    GeneratedFiles(
      entities = EntitiesGenerator(modelWithoutFiles,dbConf),
      fileTables = EntitiesGenerator(modelWithOnlyFilesTables,dbConf),
      generatedRoutes = RoutesGenerator(calculatedViews,calculatedTables,dbModel),
      entityActionsRegistry = EntityActionsRegistryGenerator(calculatedViews,calculatedTables,dbModel),
      fileAccessGenerator = FileAccessGenerator(dbModel,dbConf)
    )

  }

  def main(args: Array[String]):Unit = {

    val files = generatedFiles()

    files.entities.writeToFile(
      "ch.wsl.box.rest.jdbc.PostgresProfile",
      args(0),
      "ch.wsl.box.model",
      "Entities",
      "Entities.scala"
    )

    files.fileTables.writeToFile(
      "ch.wsl.box.rest.jdbc.PostgresProfile",
      args(0),
      "ch.wsl.box.model",
      "FileTables",
      "FileTables.scala"
    )


    files.generatedRoutes.writeToFile(
      args(0),
      "ch.wsl.box.rest.routes",
      "GeneratedRoutes",
      "GeneratedRoutes.scala",
      "ch.wsl.box.model.Entities"
    )

    files.entityActionsRegistry.writeToFile(
      args(0),
      "ch.wsl.box.model",
      "EntityActionsRegistry.scala",
      "ch.wsl.box.model.Entities"
    )

    files.fileAccessGenerator.writeToFile(
      args(0),
      "ch.wsl.box.rest.routes",
      "FileRoutes",
      "FileRoutes.scala",
      "ch.wsl.box.model.FileTables"
    )



  }




}

