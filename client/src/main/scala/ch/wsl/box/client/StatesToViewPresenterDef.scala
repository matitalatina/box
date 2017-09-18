package ch.wsl.box.client

import ch.wsl.box.client.routes.Routes
import io.udash._
import ch.wsl.box.client.views._

class StatesToViewPresenterDef extends ViewPresenterRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewPresenter[_ <: RoutingState] = state match {
    case RootState => RootViewPresenter
    case IndexState => IndexViewPresenter
    case LoginState => LoginViewPresenter
    case ModelsState(kind,model) => ModelsViewPresenter(kind,model,2)
    case ModelTableState(kind,model) => ModelTableViewPresenter(Routes(kind,model))
    case ModelFormState(kind,model,id) => ModelFormViewPresenter
    case MasterChildState(_,master,child) => MasterChildViewPresenter(master,child)
    case _ => ErrorViewPresenter
  }
}