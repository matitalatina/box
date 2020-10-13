package ch.wsl.box.client

import ch.wsl.box.client.services.ServiceModule
import io.udash.Application
import wvlet.airframe.Design

object Context {
  implicit val executionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue
  val routingRegistry = new RoutingRegistryDef
  private val viewPresenterRegistry = new StatesToViewPresenterDef
  val applicationInstance = new Application[RoutingState](routingRegistry, viewPresenterRegistry)   //udash application
  def services:ServiceModule = if(_services == null) {
    throw new Exception("Context not yet initializated, call Context.init before using it")
  }else { _services }
  private var _services:ServiceModule = null
  def init(design:Design) = {
    design.build[ServiceModule]{ sm => _services = sm}
  }

}