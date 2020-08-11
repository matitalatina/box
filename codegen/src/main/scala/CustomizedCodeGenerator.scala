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
                       fileAccessGenerator: FileAccessGenerator,
                       registry: RegistryGenerator,
                       fieldRegistry: FieldAccessGenerator,
                       tableRegistry:TableRegistryGenerator
                         )

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
    val calculatedTables = enabledTables.map(_.name.name).distinct

    GeneratedFiles(
      entities = EntitiesGenerator(dbModel,dbConf),
      fileTables = EntitiesGenerator(modelWithOnlyFilesTables,dbConf),
      generatedRoutes = RoutesGenerator(calculatedViews,calculatedTables,dbModel),
      entityActionsRegistry = EntityActionsRegistryGenerator(calculatedViews,calculatedTables,dbModel),
      fileAccessGenerator = FileAccessGenerator(dbModel,dbConf),
      registry = RegistryGenerator(dbModel),
      fieldRegistry = FieldAccessGenerator(calculatedTables ++ calculatedViews,dbModel),
      tableRegistry = TableRegistryGenerator(calculatedTables ++ calculatedViews,dbModel)
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

    files.fileTables.writeToFile(
      "ch.wsl.box.jdbc.PostgresProfile",
      args(0),
      "ch.wsl.box.generated",
      "FileTables",
      "FileTables.scala"
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
      "FileTables"
    )

    files.fieldRegistry.writeToFile(args(0),"ch.wsl.box.generated","GenFieldRegistry.scala","")

    files.tableRegistry.writeToFile(args(0),"ch.wsl.box.generated","GenTableRegistry.scala","")


    files.registry.writeToFile(args(0),"ch.wsl.box.generated","","GenRegistry.scala")




  }




}

