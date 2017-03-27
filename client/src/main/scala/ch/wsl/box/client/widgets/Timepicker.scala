package ch.wsl.box.client.widgets

import ch.wsl.box.client.components.base.widget.{Widget, WidgetProps}
import ch.wsl.box.client.libraries.JQueryTimepicker
import ch.wsl.box.model.shared.WidgetsNames
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._



/**
  * Created by andre on 3/7/2017.
  */


object Timepicker extends Widget{

  private val className = "widget-pickatime"

  override def name: String = WidgetsNames.timepicker

  def onChange(wp:WidgetProps)(e: ReactEventFromInput): Callback = Callback{
    wp.onChange(e.target.value)
  }

  override def render: (WidgetProps) => VdomElement = { P =>
    <.input(^.`type` := "text", ^.`class` := className, ^.defaultValue := P.value.map(_.toString).getOrElse(""), ^.onChange ==> onChange(P), ^.onInput ==> onChange(P))
  }

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback{
    JQueryTimepicker(className)
  } >> Callback.log("JQuery timepicker mounted")
}
