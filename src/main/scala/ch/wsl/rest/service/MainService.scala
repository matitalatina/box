package ch.wsl.rest.service


import ch.wsl.rest.service.Auth.CustomUserPassAuthenticator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import akka.actor.Actor
import spray.http.MediaTypes.{ `text/html` }
import spray.routing.Directive.pimpApply
import spray.routing.{MethodRejection, Route, HttpService, RejectionHandler}
import spray.routing.authentication.BasicAuth
import spray.routing.directives.AuthMagnet.fromContextAuthenticator

import ch.wsl.model.tables._



// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MainServiceActor extends Actor with MainService  {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  
  implicit val myRejectionHandler = RejectionHandler {
    case t => {
      println(t)
      complete("Something went wrong here: " + t)
    }
    case _ => complete("Something went wrong here")
  }

  
  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(s4Route)
}




/**
 *  this trait defines our service behavior independently from the service actor
 */
trait MainService extends HttpService with CORSSupport with ModelRoutes with ViewRoutes with GeneratedRoutes {
  


  
  
  val index = get { ctx =>
          respondWithMediaType(`text/html`) {  // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  <h1>Backend is running :-)</h1>
                </body>
              </html>
            }
          }
        }
  
  val s4Route:Route = {
    
      import JsonProtocol._

    
      pathEnd {
        index
      } ~
      cors{
        options {
           complete(spray.http.StatusCodes.OK)
        } ~
        authenticate(BasicAuth(CustomUserPassAuthenticator, "person-security-realm")) { userProfile =>
            implicit val db = userProfile.db
          generatedRoutes() ~
          path("models") {
            get{
              complete(models ++ views)
            }
          }
        }
      }
    
  }


}

