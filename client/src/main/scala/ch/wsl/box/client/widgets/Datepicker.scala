package ch.wsl.box.client.widgets


import ch.wsl.box.client.components.base.widget.{WidgetProps, Widget}
import ch.wsl.box.model.shared.WidgetsNames
import japgolly.scalajs.react.{Callback, ReactElement}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.Node

import scala.scalajs.js


/**
  * Created by andreaminetti on 06/06/16.
  */
object Datepicker extends Widget {


  override def name: String = WidgetsNames.datepicker

  private val className = "widget-pickadate"

  override def render: (WidgetProps) => ReactElement = { P =>


    <.input(^.`type` := "text", ^.`class` := className, ^.value := P.value.toString)

  }

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback{


    val opts = new PikadayOptions {
      override def onSelect(): Unit = {
        org.scalajs.dom.console.log("Selected")
      }
      override val field: Node = org.scalajs.dom.document.getElementsByClassName(className).item(0)
      override val format: String = "D MMM YYYY"
    }.toDict()

    new Pikaday(opts)
  } >> Callback.log("Pikaday mounted")
}

trait PikadayOptions{
  val field:Node
  val format:String
  def onSelect():Unit

  def toDict():js.Dictionary[js.Any] = {
    val dict:js.Dictionary[js.Any] = js.Dictionary()
    dict.update("field",field)
    dict.update("format",format)
    dict.update("onSelect",onSelect)
    dict
  }
}

@js.native
class Pikaday(opts:js.Dictionary[js.Any]) extends js.Object {

}