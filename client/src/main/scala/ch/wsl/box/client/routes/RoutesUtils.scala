package ch.wsl.box.client.routes

import ch.wsl.box.client.components.WindowComponent
import ch.wsl.box.client.controllers.{Controller, Container}
import japgolly.scalajs.react.extra.router.RouterCtl

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