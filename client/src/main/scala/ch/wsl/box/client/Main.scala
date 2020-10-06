package ch.wsl.box.client

import ch.wsl.box.client.styles.OpenLayersStyles
import ch.wsl.box.client.utils._
import io.udash.wrappers.jquery._
import org.scalajs.dom.{Element, document}
import scribe.{Level, Logger, Logging}

object Main extends Logging {
  import Context._

  def main(args: Array[String]): Unit = {


    println(
      s"""
         |===================================
         |
         |    _/_/_/      _/_/    _/      _/
         |   _/    _/  _/    _/    _/  _/
         |  _/_/_/    _/    _/      _/
         | _/    _/  _/    _/    _/  _/
         |_/_/_/      _/_/    _/      _/
         |
         |===================================
         |
         |Box client started
         |
         |""".stripMargin)

    //loads datetime picker
    typings.bootstrap.bootstrapRequire

    //require font
    styles.fonts.ClearSans

    Logger.root.clearHandlers().clearModifiers().withHandler(minimumLevel = Some(Level.Debug)).replace()

    for {
      _ <- ClientConf.load()
      _ <- Labels.load(Session.lang())
      _ <- UI.load()
    } yield {

      jQ(document).ready((_: Element) => {
        jQ("title").html(UI.title.getOrElse(s"Box").toString)
        val appRoot = jQ("#application").get(0)
        if (appRoot.isEmpty) {
          logger.error("Application root element not found! Check your index.html file!")
        } else {
          applicationInstance.run(appRoot.get)

          import scalacss.Defaults._
          import ch.wsl.box.client.styles.GlobalStyles


          jQ(s"<style>${ClientConf.style.render(cssStringRenderer,cssEnv)}</style>").insertBefore(appRoot.get)
          jQ(s"<style>${OpenLayersStyles.render(cssStringRenderer,cssEnv)}</style>").insertBefore(appRoot.get)
        }
      })
    }


  }
}
