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
    case "/box" /:/ kind /:/ model /:/ "insert" => ModelFormState(kind,model,None)
    case "/box" /:/ kind /:/ model /:/ "update" /:/ id => ModelFormState(kind,model,Some(id))
    case "/box" /:/ kind /:/ model /:/ "child" /:/ childModel => MasterChildState(kind,model,childModel)
    case "/box" /:/ kind /:/ model => ModelTableState(kind,model)
    case "/fire" => FireState
    case "/fire/insert" => FireFormState(None)
    case "/fire" /:/ id => FireFormState(Some(id))
  }
}