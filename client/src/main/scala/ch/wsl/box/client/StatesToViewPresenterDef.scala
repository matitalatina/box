package ch.wsl.box.client

import io.udash._
import ch.wsl.box.client.views._

class StatesToViewPresenterDef extends ViewPresenterRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewPresenter[_ <: RoutingState] = state match {
    case RootState => RootViewPresenter
    case IndexState => IndexViewPresenter
    case ModelsState => ModelsViewPresenter
    case ModelTableState(model) => ModelTableViewPresenter
    case _ => ErrorViewPresenter
  }
}