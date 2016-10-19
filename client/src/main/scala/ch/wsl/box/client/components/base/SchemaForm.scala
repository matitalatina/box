package ch.wsl.box.client.components.base

import ch.wsl.box.client.components.base.Debug.DebugComponent
import ch.wsl.box.client.components.base.widget.Widget
import ch.wsl.box.client.css.CommonStyles
import ch.wsl.box.client.utils.Log
import ch.wsl.box.model.shared.JSONSchemaUI
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLInputElement

import scala.scalajs.js
import scala.scalajs.js.{Any, UndefOr}

/**
  * Created by andreaminetti on 24/02/16.
  */
object SchemaForm {


  case class Props(schema:String, ui:JSONSchemaUI, onSubmit: SchemaFormState => Unit, formData:Option[js.Any] = None)


  val debugRefKey = "debugRef"
  val debugRef = Ref.to(Debug.component,debugRefKey)

  class Backend($: BackendScope[Props, Unit]) {

    def onChange(formState:SchemaFormState):Unit = {
      debugRef($).foreach(_.backend.change(Some(formState.formData)))
    }


    def render(P:Props) = {
      <.div(CommonStyles.card,
        SchemaFormNative(P.schema,Some(P.ui),onSubmit = Some(P.onSubmit),formData = P.formData, onChange = Some(onChange), widgets = Some(Widget()))(
          <.div(CommonStyles.action,
            <.button(CommonStyles.button,^.`type` := "submit","Submit")
          )
        ),
        <.hr(),
        Debug(ref=debugRefKey)
      )
    }
  }

  val component = ReactComponentB[Props]("SchemaForm")
    .renderBackend[Backend]
    .componentDidMountCB(Widget.mount)
    .build


  def apply(props: Props, ref: UndefOr[String] = "", key: Any = {}) = component.set(key, ref)(props)

}
