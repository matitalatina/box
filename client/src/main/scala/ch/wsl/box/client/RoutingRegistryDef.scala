package ch.wsl.box.client

import ch.wsl.box.client.utils.{ Session}
import io.udash._
import io.udash.utils.Bidirectional
import Context._
import slogging.LazyLogging

class RoutingRegistryDef extends RoutingRegistry[RoutingState] with LazyLogging {
  def matchUrl(url: Url): RoutingState = {
    logger.info(s"match URL ${Session.isSet(Session.USER)}")
    Session.isSet(Session.USER) match {
      case true => loggedInUrl2State.applyOrElse (url.value.stripSuffix ("/"), (x: String) => ErrorState)
      case false => loggedOutUrl2State.applyOrElse (url.value.stripSuffix ("/"), (x: String) => ErrorState)
    }
  }

  def matchState(state: RoutingState): Url = {
    logger.info(s"match STATE ${Session.isSet(Session.USER)}")
    Session.isSet(Session.USER) match {
      case true => Url(loggedInState2Url.apply(state))
      case false => Url(loggedOutState2Url.apply(state))
    }
  }


  private val (loggedInUrl2State, loggedInState2Url) = Bidirectional[String, RoutingState] {
    case "/home" => IndexState
    case "/entities" => EntitiesState("entity","")
//    case "/boxtables" => EntitiesState("table","")
    case "/tables" => EntitiesState("table","")
    case "/views" => EntitiesState("view","")
    case "/forms" => EntitiesState("form","")
    case "/box" /:/ kind /:/ entity /:/ "insert" => EntityFormState(kind,entity,None)
    case "/box" /:/ kind /:/ entity /:/ "update" /:/ id => EntityFormState(kind,entity,Some(id))
    case "/box" /:/ kind /:/ entity /:/ "child" /:/ childEntity => MasterChildState(kind,entity,childEntity)
    case "/box" /:/ kind /:/ entity => EntityTableState(kind,entity)
  }

  private val (loggedOutUrl2State, loggedOutState2Url) = Bidirectional[String, RoutingState] {
    case "" => LoginState
  }
}