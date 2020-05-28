package ch.wsl.box.client

import ch.wsl.box.client.styles.constants.StyleConstants.Colors
import ch.wsl.box.client.utils._
import io.udash._
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash.properties.PropertyCreator
import io.udash.wrappers.jquery._
import org.scalajs.dom
import org.scalajs.dom.{Element, document}
import scalatags.Text.TypedTag
import scribe.{Level, Logger, Logging}

import scala.concurrent.Future
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object Context {
  implicit val executionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue
  val routingRegistry = new RoutingRegistryDef
  private val viewPresenterRegistry = new StatesToViewPresenterDef

  implicit val applicationInstance = new Application[RoutingState](routingRegistry, viewPresenterRegistry)   //udash application
}

object Init extends JSApp with Logging {
  import Context._

  @JSExport
  override def main(): Unit = {

    Logger.root.clearHandlers().clearModifiers().withHandler(minimumLevel = Some(Level.Debug)).replace()


    logger.debug("Box started")

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
        }
      })
    }


  }
}
