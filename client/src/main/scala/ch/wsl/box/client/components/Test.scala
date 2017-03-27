package ch.wsl.box.client.components

import ch.wsl.box.client.components.base.SchemaFormNative
import ch.wsl.box.client.components.base.widget.Widget
import ch.wsl.box.client.libraries.JQueryTimepicker
import ch.wsl.box.model.shared.{JSONSchemaUI, UIWidget}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Tests {


  val schema = """{"type":"object","title":"val_a","properties":{"id":{"type":"number","title":"id","readonly":false,"order":1},"name":{"type":"string","title":"name","readonly":false,"order":2}},"required":["id"],"readonly":false}""".stripMargin
  val ui = JSONSchemaUI(Vector("id", "name"),Map("name" -> UIWidget("textinput")))

  val component = ScalaComponent.buildStatic("Item1",
    <.div(
      <.h1("Home Page"),
      <.input(^.`type` := "text", ^.`class` := "time")
    )
  ).componentDidMount{ p =>
    Callback{
      JQueryTimepicker("time")
    }
  }.build

  def apply() = component()
}
