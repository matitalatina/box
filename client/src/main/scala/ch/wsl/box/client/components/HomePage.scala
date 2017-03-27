package ch.wsl.box.client.components

import ch.wsl.box.client.components.base.SchemaFormNative
import ch.wsl.box.client.components.base.widget.Widget
import ch.wsl.box.model.shared.{JSONSchemaUI, UIWidget}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object HomePage {


  val schema = """{"type":"object","title":"val_a","properties":{"id":{"type":"number","title":"id","readonly":false,"order":1},"name":{"type":"string","title":"name","readonly":false,"order":2}},"required":["id"],"readonly":false}""".stripMargin
  val ui = JSONSchemaUI(Vector("id", "name"),Map("name" -> UIWidget("textinput")))

  val component = ScalaComponent.buildStatic("Item1",
    <.div(
      <.h1("Home Page"),
      SchemaFormNative(
        SchemaFormNative.Props(schema,Some(ui),onSubmit = None,formData = None, onChange = None, widgets = Some(Widget()))
        //          ,{
        //            <.div(CommonStyles.action,
        //              <.button(CommonStyles.button,^.`type` := "submit","Submit")
        //            )
        //          }
      )
    )
  ).componentDidMount( p => Widget.mount).build

  def apply() = component()
}
