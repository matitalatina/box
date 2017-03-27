package ch.wsl.box.client.widgets

import ch.wsl.box.client.components.base.widget.{Widget, WidgetProps}
import ch.wsl.box.model.shared.WidgetsNames
import chandu0101.scalajs.react.components.reactselect.{Select, ValueOption}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._


import scala.scalajs.js

/**
  * Created by andre on 3/13/2017.
  */
object SelectWidget extends Widget {
  override def name: String = WidgetsNames.select

  def onChange(wp:WidgetProps)(value: js.Any): Callback = Callback{
    wp.onChange(value)
  }

  override def render: (WidgetProps) => VdomElement = { P =>

//    val options = js.Array[ValueOption[ReactNode]](
//      ValueOption(value = "value1", label = "label1"),
//      ValueOption(value = 1, label = "label2"),
//      ValueOption(value = "value3", label = "label3"),
//      ValueOption(value = "value4", label = "label4"),
//      ValueOption(value = "value5", label = "label5")
//    )

    <.div(
//      Select[ReactNode](
//        options = options
//      )()
    )



  }

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback.log("Select mounted")
}
