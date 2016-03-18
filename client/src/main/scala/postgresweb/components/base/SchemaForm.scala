package postgresweb.components.base

import ch.wsl.model.shared.JSONSchemaUI
import io.circe.scalajs._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.components.base.form.Input
import postgresweb.css.CommonStyles

import scala.scalajs.js
import scala.scalajs.js.{Any, UndefOr}
import scalacss.Defaults._
import scalacss.ScalaCssReact._

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
