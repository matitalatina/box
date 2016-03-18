package ch.wsl.box.client.components.navigation

import ch.wsl.box.client.controllers.Controller
import ch.wsl.box.client.css.CommonStyles
import ch.wsl.box.client.services.Auth
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js.{Any, UndefOr}
import scalacss.Defaults._
import scalacss.ScalaCssReact._

import scala.concurrent.ExecutionContext.Implicits.global

object LeftNav {

  object Style extends StyleSheet.Inline {

    import dsl._

    val nav = style(addClassNames("mdl-layout__drawer"))

  }

  case class Props(ctrl: Controller)

  case class State(elements: Vector[String], title:String)

  class Backend(scope:BackendScope[Props,State]) {


    def logout(p:Props):Callback = {
      Auth.logout() >>
      p.ctrl.routeTo(p.ctrl.homeContainer)
    }

    def render(p:Props,s:State) = {

      if(s.title != p.ctrl.leftMenuTitle) {
        p.ctrl.leftMenu.foreach { models =>
          scope.modState(_.copy(elements = models.sorted, title = p.ctrl.leftMenuTitle)).runNow()
        }
      }

      <.div(Style.nav,
        <.span(CommonStyles.title, p.ctrl.leftMenuTitle),
        <.nav(CommonStyles.navigation,
          s.elements.map(e => <.a(CommonStyles.navigationLink, e, ^.onClick --> p.ctrl.leftMenuClick(e)))
        ),
        <.div(
          <.button(CommonStyles.button,"Logout",^.onClick --> logout(p))
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
