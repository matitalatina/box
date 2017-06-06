package ch.wsl.box.client

import ch.wsl.box.client.custom.{Fire, FireForm}
import ch.wsl.box.client.routes.Routes
import io.udash._
import ch.wsl.box.client.views._

class StatesToViewPresenterDef extends ViewPresenterRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewPresenter[_ <: RoutingState] = state match {
    case RootState => RootViewPresenter
    case IndexState => IndexViewPresenter
    case ModelsState(kind,model) => ModelsViewPresenter(kind,model,2)
    case ModelTableState(kind,model) => ModelTableViewPresenter(Routes(kind,model))
    case ModelFormState(kind,model,id) => ModelFormViewPresenter(Routes(kind,model))
    case MasterChildState(_,master,child) => MasterChildViewPresenter(master,child)
    case FireState => Fire.FireViewPresenter
    case FireFormState(_) => FireForm.FireViewPresenter
    case _ => ErrorViewPresenter
  }
}