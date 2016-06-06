package ch.wsl.box.client.widgets

/**
  * Created by andreaminetti on 06/06/16.
  */
import ch.wsl.box.client.components.base.widget.{WidgetProps, Widget}
import ch.wsl.box.model.shared.WidgetsNames
import japgolly.scalajs.react.{Callback, ReactElement}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.Node

import scala.scalajs.js


object Text extends Widget {


  override def name: String = WidgetsNames.textinput

  override def render: (WidgetProps) => ReactElement = { P =>



    println("test input render")
    js.Dynamic.global.console.log(P)

    <.input(^.`type` := "text", ^.value := P.value.toString)

  }

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback.log("Input mounted")
}

