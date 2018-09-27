package ch.wsl.box.client

import ch.wsl.box.client.routes.Routes
import io.udash._
import ch.wsl.box.client.views._

class StatesToViewPresenterDef extends ViewFactoryRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewFactory[_ <: RoutingState] = state match {
    case RootState => RootViewPresenter
    case IndexState => IndexViewPresenter
    case LoginState => LoginViewPresenter
    case EntitiesState(kind,currentEntity) => EntitiesViewPresenter(kind,currentEntity,2)
    case EntityTableState(kind,entity) => EntityTableViewPresenter(Routes(kind,entity))
    case EntityFormState(kind,entity,write,id) => EntityFormViewPresenter
    case MasterChildState(_,master,child) => MasterChildViewPresenter(master,child)
    case ExportState(_) => ExportViewPresenter
    case ExportsState(currentExport) => ExportsViewPresenter(currentExport)
    case _ => ErrorViewPresenter
  }
}