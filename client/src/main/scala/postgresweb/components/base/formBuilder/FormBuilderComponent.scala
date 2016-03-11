package postgresweb.components.base.formBuilder

import japgolly.scalajs.react.{ReactComponentB, BackendScope}
import postgresweb.css.CommonStyles
import japgolly.scalajs.react.vdom.prefix_<^._

import scalacss.Defaults._
import scalacss.ScalaCssReact._

/**
  * Created by andreaminetti on 07/03/16.
  */
object FormBuilderComponent {

  object Styles extends StyleSheet.Inline{
    import dsl._
    val textfield = style(addClassNames("mdl-textfield","mdl-js-textfield"))
    val input = style(addClassNames("mdl-textfield__input"))
    val label = style(addClassNames("mdl-textfield__label"))
  }

  case class State(columns:Int,rows:Int)


  class Backend(scope:BackendScope[Unit,State]) {


    /*
      * BASE MATERIAL LITE TEMPLATE
      *<div class="mdl-textfield mdl-js-textfield">
      *<input class="mdl-textfield__input" type="text" id="sample1">
      *<label class="mdl-textfield__label" for="sample1">Text...</label>
      *</div>
      */

    def render(s:State) = {
      <.div(CommonStyles.fullWidth,
        <.div(Styles.textfield,
          <.input(Styles.input),
          <.label(Styles.label, "Title")
        ),
        <.div(Styles.textfield,
          <.input(Styles.input, ^.`type`:= "number"),
          <.label(Styles.label, "Columns")
        ),
        <.div(Styles.textfield,
          <.input(Styles.input, ^.`type`:= "number"),
          <.label(Styles.label, "Rows")
        )
      )
    }
  }

  val component = ReactComponentB[Unit]("FormBuilderComponent")
    .initialState(State(1,1))
    .renderBackend[Backend]
    .buildU


  def apply() = component()


}
