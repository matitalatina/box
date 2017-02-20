package ch.wsl.box.client.widgets


import ch.wsl.box.client.components.base.widget.{Widget, WidgetProps}
import ch.wsl.box.client.libraries.{Pikaday, PikadayOptions}
import ch.wsl.box.model.shared.WidgetsNames
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.Node
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Any
import scala.util.Random


/**
  * Created by andreaminetti on 06/06/16.
  */
object Datepicker extends Widget {

  override def name: String = WidgetsNames.datepicker

  private val className = "widget-pickadate"

  def onChange(wp:WidgetProps)(e: ReactEventI): Callback = Callback{
    wp.onChange(e.target.value)
  }



  override def render: (WidgetProps) => ReactElement = { P =>
    //wp = Some(P)
    <.input(^.id := P.id ,^.`type` := "text", ^.`class` := className, ^.defaultValue := P.value.map(_.toString).getOrElse(""), ^.onChange ==> onChange(P), ^.onInput ==> onChange(P))
  }



  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback{
    Pikaday(className,"YYYY-MM-DD")
  } >> Callback.log("Pikaday mounted")
}