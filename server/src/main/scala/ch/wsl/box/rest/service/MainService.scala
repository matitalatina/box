package ch.wsl.box.rest.service

import spray.http.StatusCodes
import akka.actor.Actor
import spray.routing.{HttpService, RejectionHandler}




// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MainService extends Actor with HttpService with RouteRoot  {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // Handle HTTP errors, like page not found, etc.
  implicit val myRejectionHandler = RejectionHandler {
    case Nil ⇒ complete(StatusCodes.NotFound, "The requested resource could not be found.")
    case t => {
      println(t)
      complete(StatusCodes.BadRequest,"Something went wrong here: " + t)
    }
  }

  
  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)




}

