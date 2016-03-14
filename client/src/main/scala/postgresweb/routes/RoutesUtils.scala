package postgresweb.routes

import japgolly.scalajs.react.ReactComponentU
import japgolly.scalajs.react.extra.router.{RouterCtl, RouterConfigDsl}
import japgolly.scalajs.react.extra.router.StaticDsl.{Route, RouteB}
import postgresweb.components.WindowComponent
import postgresweb.controllers.Controller

/**
  * Created by andreaminetti on 14/03/16.
  */
object RoutesUtils{


  def renderController(controller:Controller,container:Container)(r:RouterCtl[Container]) = {
    controller.setContainer(container)
    controller.setRouteController(r)
    WindowComponent(props = WindowComponent.Props(controller))
  }

  def renderControllerWithModel(controller:Controller)(r:RouterCtl[Container],container: Container) = {
    controller.setContainer(container)
    controller.setRouteController(r)
    WindowComponent(props = WindowComponent.Props(controller))
  }


}