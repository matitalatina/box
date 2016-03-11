package postgresweb.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.css.CommonStyles
import postgresweb.routes.FormContainer
import postgresweb.services.ModelClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.{Any, UndefOr}
import scalacss.Defaults._
import scalacss.ScalaCssReact._

object LeftNav {

  object Style extends StyleSheet.Inline {

    import dsl._

    val nav = style(addClassNames("mdl-layout__drawer"))

  }

  case class Props(title: String, ctrl: RouterCtl[FormContainer])

  case class State(elements: Vector[String])

  class Backend(scope:BackendScope[Props,State]) {


    ModelClient.models().foreach { models =>
      scope.modState(_.copy(elements = models.sorted)).runNow()
    }

    def click(table:String)(e:ReactEventI):Callback = Callback.log(table)

    def render(p:Props,s:State) =
      <.div(Style.nav,
        <.span(CommonStyles.title,p.title),
        <.nav(CommonStyles.navigation,
          s.elements.map(e => <.a(CommonStyles.navigationLink, e, p.ctrl setOnClick FormContainer.Table(e)))
        )
      )
  }


  val component = ReactComponentB[Props]("LeftNav")
    .initialState(State(Vector()))
    .renderBackend[Backend]
    .build


  def apply(props: Props, ref: UndefOr[String] = "", key: Any = {}) = component.set(key, ref)(props)

}
