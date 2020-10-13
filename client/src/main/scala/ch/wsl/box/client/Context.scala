package ch.wsl.box.client

import io.udash.Application

class Context {
  implicit val executionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue
  val routingRegistry = new RoutingRegistryDef
  private val viewPresenterRegistry = new StatesToViewPresenterDef

  val applicationInstance = new Application[RoutingState](routingRegistry, viewPresenterRegistry)   //udash application
}