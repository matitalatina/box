package ch.wsl.box.client.routes

import ch.wsl.box.client.{ModelFormState, ModelTableState}

/**
  * Created by andre on 6/6/2017.
  */
case class Routes(kind:String,model:String) {
  def add() = ModelFormState(kind,model,None)
  def edit(id:String) = ModelFormState(kind,model,Some(id))
  def table() = ModelTableState(kind,model)
}

