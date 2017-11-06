package ch.wsl.box.client

import ch.wsl.box.client.utils.{Conf, Labels, Session}
import io.udash._
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash.properties.PropertyCreator
import io.udash.wrappers.jquery._
import org.scalajs.dom.{Element, document}

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object Context {
  implicit val executionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue
  private val routingRegistry = new RoutingRegistryDef
  private val viewPresenterRegistry = new StatesToViewPresenterDef

  implicit val applicationInstance = new Application[RoutingState](routingRegistry, viewPresenterRegistry, RootState)   //udash application

  implicit val pc: PropertyCreator[Option[ch.wsl.box.model.shared.JSONMetadata]] = PropertyCreator.propertyCreator[Option[ch.wsl.box.model.shared.JSONMetadata]]

}

object Init extends JSApp with StrictLogging {
  import Context._

  @JSExport
  override def main(): Unit = {

    for {
      _ <- Conf.load()
      _ <- Labels.load(Session.lang())
    } yield {
      jQ(document).ready((_: Element) => {
        val appRoot = jQ("#application").get(0)
        if (appRoot.isEmpty) {
          logger.error("Application root element not found! Check your index.html file!")
        } else {
          applicationInstance.run(appRoot.get)

          import scalacss.Defaults._
          import scalacss.ScalatagsCss._
          import scalatags.JsDom._
          import ch.wsl.box.client.styles.GlobalStyles
          import ch.wsl.box.client.styles.partials.FooterStyles
          jQ(GlobalStyles.render[TypedTag[org.scalajs.dom.raw.HTMLStyleElement]].render).insertBefore(appRoot.get)
          jQ(FooterStyles.render[TypedTag[org.scalajs.dom.raw.HTMLStyleElement]].render).insertBefore(appRoot.get)
          jQ(UdashDatePicker.loadBootstrapDatePickerStyles()).insertBefore(appRoot.get)
        }
      })
    }


  }
}