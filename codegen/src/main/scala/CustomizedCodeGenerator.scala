package ch.wsl.box.codegen


/**
 *  This customizes the Slick code generator.
 *  For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
 */
object CustomizedCodeGenerator extends BaseCodeGenerator {
  def main(args: Array[String]):Unit = {



    TablesGenerator(model,dbConf).writeToFile(
      "slick.driver.PostgresDriver",
      args(0),
      "ch.wsl.box.model",
      "Tables",
      "Tables.scala"
    )




    val calculatedViews = enabledViews.map(_.name.name)
    val calculatedTables= enabledTables.map(_.name.name)


    RoutesGenerator(calculatedViews,calculatedTables,model).writeToFile(
      args(0),
      "ch.wsl.box.rest.routes",
      "GeneratedRoutes",
      "GeneratedRoutes.scala",
      "ch.wsl.box.model.tables"
    )

    RegistryModelsGenerator(calculatedViews,calculatedTables,model).writeToFile(
      args(0),
      "ch.wsl.box.model",
      "TablesRegistry.scala"
    )

    println("Exit")


  }




}

