package ch.wsl.box.client

import ch.wsl.box.client.utils.Session
import io.udash._
import io.udash.utils.Bidirectional

class RoutingRegistryDef extends RoutingRegistry[RoutingState] {
  def matchUrl(url: Url): RoutingState = {
    println(s"match URL ${Session.isset(Session.USER)}")
    Session.isset(Session.USER) match {
      case true => loggedInUrl2State.applyOrElse (url.value.stripSuffix ("/"), (x: String) => ErrorState)
      case false => loggedOutUrl2State.applyOrElse (url.value.stripSuffix ("/"), (x: String) => ErrorState)
    }
  }

  def matchState(state: RoutingState): Url = {
    println(s"match STATE ${Session.isset(Session.USER)}")
    Session.isset(Session.USER) match {
      case true => Url(loggedInState2Url.apply(state))
      case false => Url(loggedOutState2Url.apply(state))
    }
  }


  private val (loggedInUrl2State, loggedInState2Url) = Bidirectional[String, RoutingState] {
    case "/home" => IndexState
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

  private val (loggedOutUrl2State, loggedOutState2Url) = Bidirectional[String, RoutingState] {
    case "" => LoginState
  }
}