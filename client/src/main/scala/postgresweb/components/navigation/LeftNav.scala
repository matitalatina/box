package postgresweb.components.navigation

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.controllers.Controller
import postgresweb.css.CommonStyles
import postgresweb.routes.AppRouter
import postgresweb.services.TableClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.{Any, UndefOr}
import scalacss.Defaults._
import scalacss.ScalaCssReact._

object LeftNav {

  object Style extends StyleSheet.Inline {

    import dsl._

    val nav = style(addClassNames("mdl-layout__drawer"))

  }

  case class Props(ctrl: Controller)

  case class State(elements: Vector[String], title:String)

  class Backend(scope:BackendScope[Props,State]) {



    def render(p:Props,s:State) = {

      if(s.title != p.ctrl.leftMenuTitle) {
        p.ctrl.leftMenu.foreach { models =>
          scope.modState(_.copy(elements = models.sorted, title = p.ctrl.leftMenuTitle)).runNow()
        }
      }

      <.div(Style.nav,
        <.span(CommonStyles.title, p.ctrl.leftMenuTitle),
        <.nav(CommonStyles.navigation,
          s.elements.map(e => p.ctrl.leftMenuLink(e)(CommonStyles.navigationLink, e))
        )
      )
    }
  }


  val component = ReactComponentB[Props]("LeftNav")
    .initialState(State(Vector(),""))
    .renderBackend[Backend]
    .build


  def apply(props: Props, ref: UndefOr[String] = "", key: Any = {}) = component.set(key, ref)(props)

}
