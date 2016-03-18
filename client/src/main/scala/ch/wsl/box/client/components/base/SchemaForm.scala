package ch.wsl.box.client.components.base

import ch.wsl.box.client.css.CommonStyles
import ch.wsl.box.model.shared.JSONSchemaUI
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js
import scala.scalajs.js.{Any, UndefOr}

/**
  * Created by andreaminetti on 24/02/16.
  */
object SchemaForm {



  case class Props(schema:String, ui:JSONSchemaUI, onSubmit: SchemaFormState => Unit, formData:Option[js.Any] = None)


  val component = ReactComponentB[Props]("SchemaForm")
    .render_P { P =>
      <.div(CommonStyles.card,
        SchemaFormNative(P.schema,Some(P.ui),onSubmit = Some(P.onSubmit),formData = P.formData)(
          <.div(CommonStyles.action,
            <.button(CommonStyles.button,^.`type` := "submit","Submit")
          )
        )
      )
    }
    .build


  def apply(props: Props, ref: UndefOr[String] = "", key: Any = {}) = component.set(key, ref)(props)

}
