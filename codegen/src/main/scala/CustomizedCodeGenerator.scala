package ch.wsl.box.codegen


/**
 *  This customizes the Slick code generator.
 *  For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
 */
object CustomizedCodeGenerator extends BaseCodeGenerator {
  def main(args: Array[String]):Unit = {



    val modelWithoutFiles = dbModel.copy(tables = dbModel.tables.map { table =>
      table.copy(columns = table.columns.filterNot { c =>
        c.tpe == "Array[Byte]"
      })
    })

    EntitiesGenerator(modelWithoutFiles,dbConf).writeToFile(
      "slick.driver.PostgresDriver",
      args(0),
      "ch.wsl.box.model",
      "Tables",
      "Tables.scala"
    )


    val modelWithOnlyFilesTables = dbModel.copy(tables = dbModel.tables.filter(_.columns.exists(_.tpe == "Array[Byte]")).map{ t =>
      t.copy(foreignKeys = Seq())
    })

    EntitiesGenerator(modelWithOnlyFilesTables,dbConf).writeToFile(
      "slick.driver.PostgresDriver",
      args(0),
      "ch.wsl.box.model",
      "FileTables",
      "FileTables.scala"
    )




    val calculatedViews = enabledViews.map(_.name.name)
    val calculatedTables= enabledTables.map(_.name.name)


    RoutesGenerator(calculatedViews,calculatedTables,dbModel).writeToFile(
      args(0),
      "ch.wsl.box.rest.routes",
      "GeneratedRoutes",
      "GeneratedRoutes.scala",
      "ch.wsl.box.model.Tables"
    )

    EntityActionsRegistryGenerator(calculatedViews,calculatedTables,dbModel).writeToFile(
      args(0),
      "ch.wsl.box.model",
      "TablesRegistry.scala"
    )

    FileAccessGenerator(dbModel,dbConf).writeToFile(
      args(0),
      "ch.wsl.box.rest.routes",
      "FileRoutes",
      "FileRoutes.scala",
      "ch.wsl.box.model.FileTables"
    )

    println("Exit")


  }




}

