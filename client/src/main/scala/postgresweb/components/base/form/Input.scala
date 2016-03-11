package postgresweb.components.base.form

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js.{Any, UndefOr}
import scalacss.Defaults._
import scalacss.ScalaCssReact._

/**
  * Created by andreaminetti on 22/02/16.
  */
object Input {
  object Style extends StyleSheet.Inline {

    import dsl._
    val div = style(addClassNames("mdl-textfield","mdl-js-textfield","mdl-textfield--floating-label"))
    val input = style(addClassNames("mdl-textfield__input"))
    val label = style(addClassNames("mdl-textfield__label"))
  }


  case class Props(id:String,`type`:String, label:String)

  val component = ReactComponentB[Props]("Table")
    .render_P { P =>
      <.div(Style.div,
        <.input(Style.input, ^.`type` := P.`type`, ^.id := P.id),
        <.label(Style.label, ^.`for` := P.id,P.label)
      )
    }
    .build


  def apply(props: Props, ref: UndefOr[String] = "", key: Any = {}) = component.set(key, ref)(props)

}