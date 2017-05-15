package ch.wsl.box.client

import io.udash._
import ch.wsl.box.client.views._

class StatesToViewPresenterDef extends ViewPresenterRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewPresenter[_ <: RoutingState] = state match {
    case RootState => RootViewPresenter
    case IndexState => IndexViewPresenter
    case ModelsState(_,_) => ModelsViewPresenter
    case ModelTableState(_,model) => ModelTableViewPresenter()
    case ModelFormState(_,model,id) => ModelFormViewPresenter
    case MasterChildState(_,master,child) => MasterChildViewPresenter(master,child)
    case _ => ErrorViewPresenter
  }
}