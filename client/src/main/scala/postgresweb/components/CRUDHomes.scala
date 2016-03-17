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
case class CRUDHomes(ctrl: Controller) {



  case class State(elements: Vector[String])

  class Backend(scope:BackendScope[Unit,State]) {


    ctrl.leftMenu.foreach { models =>
      scope.modState(_.copy(elements = models.sorted)).runNow()
    }

    def render(s:State) =
      <.div(
        <.h1(ctrl.leftMenuTitle),
        <.ul(
          s.elements.map { e =>
            <.li(
              ctrl.leftMenuLink(e)(CommonStyles.navigationLink, e)
            )
          }
        )
      )
  }


  val component = ReactComponentB[Unit]("CRUDHome")
    .initialState(State(Vector()))
    .renderBackend[Backend]
    .buildU


  def apply() = component()

}
