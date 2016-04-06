package ch.wsl.box.client.components.base

import ch.wsl.box.client.components.base.widget.Widget
import ch.wsl.box.model.shared.JSONSchemaUI
import io.circe.scalajs._
import japgolly.scalajs.react.{React, ReactComponentU_, ReactNode}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.scalajs._

/**
  * Created by andreaminetti on 24/02/16.
  *
  * Wrapper of https://github.com/mozilla-services/react-jsonschema-form/blob/master/src/components/Form.js
  *
  * used wrapper guide on https://github.com/chandu0101/scalajs-react-components/blob/master/doc/InteropWithThirdParty.md
  *
  */

@js.native
trait SchemaFormState extends js.Object{
  def formData: js.Any
}


case class SchemaFormNative(
                             schema:String,
                             uiSchema:Option[JSONSchemaUI] = None,
                             formData:Option[js.Any] = None,
                             onChange:Option[() => Unit] = None,
                             onError:Option[() => Unit] = None,
                             onSubmit:Option[SchemaFormState => Unit] = None,
                             schemaField:Option[() => Unit] = None,
                             titleField:Option[() => Unit] = None,
                             widgets:Option[js.Dictionary[js.Function]] = None
  )  {



   def apply(childs: ReactNode*) = {

    val p = js.Dynamic.literal()
      p.updateDynamic("schema")(js.JSON.parse(schema)) //fix to avoid undefined fields
      uiSchema.foreach{ui =>

        val dict = js.Dictionary[js.Any]()
        dict.update("ui:order",js.JSON.parse(ui.`ui:order`.asJson.noSpaces))
        ui.widgets.foreach{ case (key,value) =>
          dict.update(key,js.JSON.parse(value.asJson.noSpaces))
        }

        p.updateDynamic("uiSchema")(dict)
      }
      formData.foreach(fd => p.updateDynamic("formData")(fd))
      onChange.foreach(oc => p.updateDynamic("onChange")(oc))
      onError.foreach(oe => p.updateDynamic("onError")(oe))
      onSubmit.foreach(os => p.updateDynamic("onSubmit")(os))
      schemaField.foreach(sf => p.updateDynamic("SchemaField")(sf))
      titleField.foreach(tf => p.updateDynamic("TitleField")(tf))
      widgets.foreach(w => p.updateDynamic("widgets")(w))


     js.Dynamic.global.console.log(p)


      val component = React.asInstanceOf[js.Dynamic].createElement(js.Dynamic.global.JSONSchemaForm.default,p,childs.toJsArray)
      js.Dynamic.global.console.log(component)
      component.asInstanceOf[ReactComponentU_]
    }
  }

