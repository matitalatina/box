package ch.wsl.box.client.widgets

/**
  * Created by andreaminetti on 06/06/16.
  */
import ch.wsl.box.client.components.base.widget.{WidgetProps, Widget}
import ch.wsl.box.model.shared.WidgetsNames
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.Node

import scala.scalajs.js


object Text extends Widget {


  override def name: String = WidgetsNames.textinput

  def onChange(wp:WidgetProps)(e: ReactEventFromInput): Callback = Callback{
    wp.onChange(e.target.value)
  }

  override def render: (WidgetProps) => VdomElement = { P =>
    println("rendering textinput")
    <.input(^.`type` := "text", ^.defaultValue := P.value.map(_.toString).getOrElse(""), ^.onChange ==> onChange(P))
  }

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback.log("Input mounted")
}

