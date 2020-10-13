package ch.wsl.box.client

import ch.wsl.box.client.services.ServiceModule
import io.udash.Application
import wvlet.airframe.Design

import scala.concurrent.ExecutionContext

object Context {
  implicit var executionContext: ExecutionContext = null
  val routingRegistry = new RoutingRegistryDef
  private val viewPresenterRegistry = new StatesToViewPresenterDef
  val applicationInstance = new Application[RoutingState](routingRegistry, viewPresenterRegistry)   //udash application
  def services:ServiceModule = if(_services == null) {
    throw new Exception("Context not yet initializated, call Context.init before using it")
  }else { _services }
  private var _services:ServiceModule = null
  def init(design:Design,ec: ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue) = {
    design.build[ServiceModule]{ sm => _services = sm}
    executionContext = ec
  }

}