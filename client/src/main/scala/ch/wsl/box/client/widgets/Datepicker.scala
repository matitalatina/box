package ch.wsl.box.client.widgets


import ch.wsl.box.client.components.base.widget.{WidgetProps, Widget}
import ch.wsl.box.model.shared.WidgetsNames
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.Node
import org.scalajs.dom.raw.{TextEvent, HTMLInputElement}
import org.widok.moment.Moment
import org.scalajs.dom

import scala.scalajs.js


/**
  * Created by andreaminetti on 06/06/16.
  */
object Datepicker extends Widget {


  override def name: String = WidgetsNames.datepicker

  private val className = "widget-pickadate"

  def onChange(wp:WidgetProps)(e: ReactEventI): Callback = Callback{
    wp.onChange(e.target.value)
  }

  private var wp:Option[WidgetProps] = None

  override def render: (WidgetProps) => ReactElement = { P =>
    wp = Some(P)
    <.input(^.`type` := "text", ^.`class` := className, ^.defaultValue := P.value.map(_.toString).getOrElse(""), ^.onChange ==> onChange(P), ^.onInput ==> onChange(P))
  }


  def opts = new PikadayOptions {
    override def onSelect:(js.Any) => Unit = (date) => {
      val event = dom.document.createEvent("HTMLEvents")
      event.initEvent("input", true, false)
      field.dispatchEvent(event)
    }
    override val field: Node = org.scalajs.dom.document.getElementsByClassName(className).item(0)
    override val format: String = "YYYY-MM-DD"
  }.toDict()

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback{
    new Pikaday(opts)
  } >> Callback.log("Pikaday mounted")
}

trait PikadayOptions{
  val field:Node
  val format:String
  def onSelect:(js.Any) => Unit

  def toDict():js.Dictionary[js.Any] = {
    val dict:js.Dictionary[js.Any] = js.Dictionary()
    dict.update("field",field)
    dict.update("format",format)
    dict.update("onSelect",onSelect)
    dict
  }
}

/**
  * https://github.com/dbushell/Pikaday
 *
  * @param opts
  */
@js.native
class Pikaday(opts:js.Dictionary[js.Any]) extends js.Object {

}