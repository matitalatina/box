package ch.wsl.box.client.components.base.form

import ch.wsl.box.client.components.base.widget.{WidgetProps, Widget}
import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js


/**
  * Created by andreaminetti on 22/02/16.
  */
object Input extends Widget {


  override def name: String = "myinputwidget"

  override def render: (WidgetProps) => ReactElement = { P =>



    println("test input render")
    js.Dynamic.global.console.log(P)

    <.input(^.`type` := "text", ^.value := P.value.toString)

  }

}