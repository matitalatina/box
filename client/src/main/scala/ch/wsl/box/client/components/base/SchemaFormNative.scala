package ch.wsl.box.client.components.base

import ch.wsl.model.shared.JSONSchemaUI
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
                             titleField:Option[() => Unit] = None
  )  {



   def apply(childs: ReactNode*) = {

    val p = js.Dynamic.literal()
      p.updateDynamic("schema")(js.JSON.parse(schema)) //fix to avoid undefined fields
      uiSchema.foreach(ui => p.updateDynamic("uiSchema")(ui.asJsAny))
      formData.foreach(fd => p.updateDynamic("formData")(fd))
      onChange.foreach(oc => p.updateDynamic("onChange")(oc))
      onError.foreach(oe => p.updateDynamic("onError")(oe))
      onSubmit.foreach(os => p.updateDynamic("onSubmit")(os))
      schemaField.foreach(sf => p.updateDynamic("SchemaField")(sf))
      titleField.foreach(tf => p.updateDynamic("TitleField")(tf))


     js.Dynamic.global.console.log(p)


      val component = React.asInstanceOf[js.Dynamic].createElement(js.Dynamic.global.JSONSchemaForm,p,childs.toJsArray)
      js.Dynamic.global.console.log(component)
      component.asInstanceOf[ReactComponentU_]
    }
  }

