package postgresweb.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.components.WindowComponent.Style._
import postgresweb.components.navigation.{LeftNav, TopNav}
import postgresweb.controllers.Controller
import postgresweb.css.CommonStyles


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js._


/**
  * Created by andreaminetti on 17/03/16.
  */
object CRUDHomes {


  case class Props(ctrl: Controller)

  case class State(elements: Vector[String])

  class Backend(scope:BackendScope[Props,State]) {


    scope.props.map{ _.ctrl.leftMenu.foreach { models =>
      scope.modState(_.copy(elements = models.sorted)).runNow()
    }
    }.runNow()

    def render(p:Props,s:State) =
      <.div(
        <.h1(p.ctrl.leftMenuTitle),
        <.ul(
          s.elements.map { e =>
            <.li(
              <.a(CommonStyles.navigationLink, e, ^.onClick --> p.ctrl.leftMenuClick(e))
            )
          }
        )
      )
  }


  val component = ReactComponentB[Props]("CRUDHome")
    .initialState(State(Vector()))
    .renderBackend[Backend]
    .build


  def apply(props: Props, ref: UndefOr[String] = "", key: Any = {}) = component.set(key, ref)(props)

}
