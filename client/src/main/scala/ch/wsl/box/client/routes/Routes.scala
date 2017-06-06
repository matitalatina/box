package ch.wsl.box.client.routes

import ch.wsl.box.client.{ModelFormState, ModelTableState, RoutingState}

/**
  * Created by andre on 6/6/2017.
  */

trait Routes{
  def add():RoutingState
  def edit(id:String):RoutingState
  def table():RoutingState
}

object Routes {

  def apply(kind:String,model:String) = new Routes{
    def add() = ModelFormState(kind,model,None)
    def edit(id:String) = ModelFormState(kind,model,Some(id))
    def table() = ModelTableState(kind,model)
  }

}

