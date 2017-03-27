package ch.wsl.box.client.components.base

import ch.wsl.box.client.components.base.widget.Widget
import ch.wsl.box.model.shared.JSONSchemaUI
import io.circe.scalajs._
import japgolly.scalajs.react._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs._
import scala.scalajs.js.annotation.JSName

/**
  * Created by andreaminetti on 24/02/16.
  *
  * Wrapper of https://github.com/mozilla-services/react-jsonschema-form/blob/master/src/components/Form.js
  *
  * used wrapper guide on https://github.com/chandu0101/scalajs-react-components/blob/master/doc/InteropWithThirdParty.md
  *
  */



object SchemaFormNative {


  @js.native
  trait State extends js.Object {
    def formData: js.Any
  }

  object State{
    def apply(formData: Option[js.Any] = None):State = {
      val p = js.Dynamic.literal()
      formData.foreach(fd => p.updateDynamic("formData")(fd))
      p.asInstanceOf[State]
    }
  }

  @js.native
  trait Props extends js.Object{
    def schema:js.Any
    def `ui:order`:js.Any
    def onChange:js.Any
    def onError:js.Any
    def onSubmit:js.Any
    def schemaField:js.Any
    def titleField:js.Any
    def widgets:js.Any
    def formData:js.Any
  }

  object Props {
    def apply(
               schema: String,
               uiSchema: Option[JSONSchemaUI] = None,
               formData: Option[js.Any] = None,
               onChange: Option[State => Unit] = None,
               onError: Option[() => Unit] = None,
               onSubmit: Option[State => Unit] = None,
               schemaField: Option[() => Unit] = None,
               titleField: Option[() => Unit] = None,
               widgets: Option[js.Dictionary[js.Function]] = None
             ):Props = {

      val p = js.Dynamic.literal()
      p.updateDynamic("schema")(js.JSON.parse(schema)) //fix to avoid undefined fields
      uiSchema.foreach { ui =>

        val dict = js.Dictionary[js.Any]()
        dict.update("ui:order", js.JSON.parse(ui.`ui:order`.asJson.noSpaces))
        ui.widgets.foreach { case (key, value) =>
          dict.update(key, js.JSON.parse(value.asJson.noSpaces))
        }

        p.updateDynamic("uiSchema")(dict)
      }
      formData.foreach(fd => p.updateDynamic("formData")(fd))
      onChange.foreach(oc => p.updateDynamic("onChange")(oc))
      onError.foreach(oe => p.updateDynamic("onError")(oe))
      onSubmit.foreach(os => p.updateDynamic("onSubmit")(os))
      schemaField.foreach(sf => p.updateDynamic("SchemaField")(sf))
      titleField.foreach(tf => p.updateDynamic("TitleField")(tf))

      //val registry = js.Dynamic.literal()
      widgets.foreach(w => p.updateDynamic("widgets")(w))
      //p.updateDynamic("registry")(registry)

      js.Dynamic.global.console.log(p)

      p.asInstanceOf[Props]

    }
  }

  def apply(p:Props) = {
    js.Dynamic.global.console.log("Mounting schema form")

    val form:js.Dynamic = js.Dynamic.global.JSONSchemaForm.default
    //val result:js.Dynamic = js.Dynamic.global.React.createElement(form,p)

    JsComponent[Props, Children.None,Null](form).apply(p)
  }


}