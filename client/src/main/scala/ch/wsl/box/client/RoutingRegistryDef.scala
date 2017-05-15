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
    case "/models" => ModelsState("model","")
    case "/forms" => ModelsState("form","")
    case kind /:/ model /:/ "insert" => ModelFormState(kind,model,None)
    case kind /:/ model /:/ "update" /:/ id => ModelFormState(kind,model,Some(id))
    case kind /:/ model /:/ "child" /:/ childModel => MasterChildState(kind,model,childModel)
    case kind /:/ model => ModelTableState(kind,model)
  }
}