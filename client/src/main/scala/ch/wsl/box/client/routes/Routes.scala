package ch.wsl.box.client.routes

import ch.wsl.box.client.{EntityFormState, EntityTableState, RoutingState}

/**
  * Created by andre on 6/6/2017.
  */

trait Routes{
  def add():RoutingState
  def edit(id:String):RoutingState
  def entity():RoutingState
  def entity(name:String):RoutingState
}

object Routes {

  def apply(kind:String, entityName:String) = new Routes{
    def add() = EntityFormState(kind,entityName,None)
    def edit(id:String) = EntityFormState(kind,entityName,Some(id))
    def entity() = EntityTableState(kind,entityName)
    def entity(name:String) = EntityTableState(kind,name)
  }

}

