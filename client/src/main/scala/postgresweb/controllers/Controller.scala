package postgresweb.controllers

import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.extra.router.RouterCtl
import postgresweb.routes.Container

/**
  * Created by andreaminetti on 14/03/16.
  */
trait Controller {

  private var _container:Container = null
  private var _routeController:RouterCtl[Container] = null

  def setRouteController(r:RouterCtl[Container]) = _routeController = r
  def routeController = _routeController

  def setContainer(c: Container): Unit = _container = c
  def container = _container

  def menu:Vector[Container] = Vector()

  def render():ReactElement = container.component

}

trait CRUDController extends Controller {
  private var _model:String = null

  def setModel(m:String) = _model = m
  def model = _model
}
