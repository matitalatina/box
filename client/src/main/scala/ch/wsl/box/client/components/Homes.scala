package ch.wsl.box.client.components

import ch.wsl.box.client.controllers.Controller
import ch.wsl.box.client.css.CommonStyles
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, _}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andreaminetti on 17/03/16.
  */
case class Homes(ctrl: Controller) {



  case class State(elements: Vector[String])

  class Backend(scope:BackendScope[Unit,State]) {


    ctrl.leftMenu.foreach { models =>
      scope.modState(_.copy(elements = models.sorted)).runNow()
    }

    def render(s:State) =
      <.div(
        <.h1(ctrl.leftMenuTitle),
        <.ul(
          s.elements.toTagMod { e =>
            <.li(
              <.a(CommonStyles.navigationLink, e, ^.onClick --> ctrl.leftMenuClick(e))
            )
          }
        )
      )
  }


  val component = ScalaComponent.build[Unit]("CRUDHome")
    .initialState(State(Vector()))
    .renderBackend[Backend]
    .build


  def apply() = component()

}
