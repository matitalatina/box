package ch.wsl.box.client

import io.udash._
import ch.wsl.box.client.views._

class StatesToViewPresenterDef extends ViewPresenterRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewPresenter[_ <: RoutingState] = state match {
    case RootState => RootViewPresenter
    case IndexState => IndexViewPresenter
    case ModelsState(_) => ModelsViewPresenter
    case ModelTableState(model) => ModelTableViewPresenter
    case ModelFormState(model,id) => ModelFormViewPresenter
    case MasterChildState(_,_) => MasterChildViewPresenter
    case _ => ErrorViewPresenter
  }
}