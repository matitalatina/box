package ch.wsl.box.client

import io.udash._
import io.udash.utils.Bidirectional

class RoutingRegistryDef extends RoutingRegistry[RoutingState] {
  def matchUrl(url: Url): RoutingState =
    url2State.applyOrElse(url.value.stripSuffix("/"), (x: String) => ErrorState)

  def matchState(state: RoutingState): Url =
    Url(state2Url.apply(state))

  private val (url2State, state2Url) = Bidirectional[String, RoutingState] {
    case "" => IndexState
    case "/models" => ModelsState
    case "/model" /:/ model /:/ "insert" => ModelInsertState(model)
    case "/model" /:/ model /:/ "update" /:/ id => ModelUpdateState(model,id)
    case "/model" /:/ model => ModelTableState(model)
  }
}