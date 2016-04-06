package ch.wsl.box.client.components.base.form

import ch.wsl.box.client.components.base.widget.Widget
import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js


/**
  * Created by andreaminetti on 22/02/16.
  */
case class InputProps(id:String,`type`:String, label:String) extends Widget.Props

object Input extends Widget[InputProps] {


  override def name: String = "myinputwidget"

  override def render: (js.Dynamic) => ReactElement = { P =>
    println("test input render")
    js.Dynamic.global.console.log(P)

    <.div("TEST")

  }

}