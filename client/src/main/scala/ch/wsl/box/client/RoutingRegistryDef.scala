package ch.wsl.box.client

import ch.wsl.box.client.services.ClientSession
import io.udash._
import scribe.Logging

class RoutingRegistryDef extends RoutingRegistry[RoutingState] with Logging {
  import Context._
  def matchUrl(url: Url): RoutingState = {
    logger.info(s"match URL $url logged: ${services.clientSession.isSet(ClientSession.USER)}")
    services.clientSession.isSet(ClientSession.USER) match {
      case true => loggedInUrl2State.applyOrElse (url.value.stripSuffix ("/"), (x: String) => ErrorState)
      case false => loggedOutUrl2State.applyOrElse (url.value.stripSuffix ("/"), (x: String) => ErrorState)
    }
  }

  def matchState(state: RoutingState): Url = {
    logger.info(s"match STATE ${services.clientSession.isSet(ClientSession.USER)}")
    services.clientSession.isSet(ClientSession.USER) match {
      case true => Url(loggedInState2Url.apply(state))
      case false => Url(loggedOutState2Url.apply(state))
    }
  }


  private val (loggedInUrl2State, loggedInState2Url) = bidirectional {
    case "/home" => IndexState
    case "/entities" => EntitiesState("entity","")
//    case "/boxtables" => EntitiesState("table","")
    case "/tables" => EntitiesState("table","")
    case "/views" => EntitiesState("view","")
    case "/forms" => EntitiesState("form","")
    case "/functions"  => DataListState(DataKind.FUNCTION,"")
    case "/exports"  => DataListState(DataKind.EXPORT,"")
    case "/box" / "export" / exportFunction  => DataState(DataKind.EXPORT,exportFunction)
    case "/box" / "function" / exportFunction  => DataState(DataKind.FUNCTION,exportFunction)
    case "/box" / kind / entity / "insert" => EntityFormState(kind,entity,"true",None)
    case "/box" / kind / entity / "row" / write / id  => EntityFormState(kind,entity,write,Some(id))
    case "/box" / kind / entity / "child" / childEntity => MasterChildState(kind,entity,childEntity)
    case "/box" / kind / entity => EntityTableState(kind,entity)
    case "/admin"  => AdminState
  }

  private val (loggedOutUrl2State, loggedOutState2Url) = bidirectional {
    case "" => LoginState
  }
}