package postgresweb.controllers


import ch.wsl.jsonmodels.{JSONQuery, JSONSchemaUI, Table}
import japgolly.scalajs.react.{Callback, ReactElement}
import japgolly.scalajs.react.extra.router.RouterCtl
import postgresweb.routes.Container

import scala.concurrent.Future
import scala.scalajs.js

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

  def menuClick(container:Container):Callback = {
    setContainer(container)
    routeController.set(container)
  }

  def render():ReactElement = container.component

}

trait CRUDController extends Controller {

  protected var filter:JSONQuery = JSONQuery.baseFilter
  protected var id:String = ""

  protected def load(jq: JSONQuery):Future[Table]

  def table:Future[Table] = load(filter)
  def schemaAsString:Future[String]
  def uiSchema:Future[JSONSchemaUI]
  def get(id:String): Future[js.Any]
  def get: Future[js.Any]

  def selectId(id:String) = this.id = id
  def setQuery(jsonQuery: JSONQuery) = filter = jsonQuery
  def query:JSONQuery = filter

  def onInsert(data:js.Any):Callback
  def onUpdate(data:js.Any):Callback

}
