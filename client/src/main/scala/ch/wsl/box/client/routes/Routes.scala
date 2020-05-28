package ch.wsl.box.client.routes

import ch.wsl.box.client.{EntityFormState, EntityTableState, RoutingState}
import org.scalajs.dom

/**
  * Created by andre on 6/6/2017.
  */

trait Routes{
  def add():RoutingState
  def edit(id:String):RoutingState
  def show(id:String):RoutingState
  def entity():RoutingState
  def entity(name:String):RoutingState
}

object Routes {

  def apiV1(path:String = ""):String = {
    dom.window.location.port == "12345" match {
      case false => "/api/v1"+path
      case true => "http://localhost:8080/api/v1" + path
    }
  }

  def apply(kind:String, entityName:String) = new Routes{
    def add() = EntityFormState(kind,entityName,"true",None)
    def edit(id:String) = EntityFormState(kind,entityName,"true",Some(id))
    def show(id:String) = EntityFormState(kind,entityName,"false",Some(id))
    def entity() = EntityTableState(kind,entityName)
    def entity(name:String) = EntityTableState(kind,name)
  }

}

