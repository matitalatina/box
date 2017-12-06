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
    case "/entities" => EntitiesState("entity","")
    case "/forms" => EntitiesState("form","")
    case "/box" /:/ kind /:/ entity /:/ "insert" => EntityFormState(kind,entity,None)
    case "/box" /:/ kind /:/ entity /:/ "update" /:/ id => EntityFormState(kind,entity,Some(id))
    case "/box" /:/ kind /:/ entity /:/ "child" /:/ childEntity => MasterChildState(kind,entity,childEntity)
    case "/box" /:/ kind /:/ entity => EntityTableState(kind,entity)
    case "/fire" => FireState
    case "/fire/insert" => FireFormState(None)
    case "/fire" /:/ id => FireFormState(Some(id))
  }

  private val (loggedOutUrl2State, loggedOutState2Url) = Bidirectional[String, RoutingState] {
    case "" => LoginState
  }
}