package ch.wsl.rest.service


import ch.wsl.rest.domain.Forms
import ch.wsl.rest.service.Auth.CustomUserPassAuthenticator
import spray.http.StatusCodes

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




/**
 *  this trait defines our service behavior independently from the service actor
 */
trait MainService extends HttpService with CORSSupport with ModelRoutes with ViewRoutes with GeneratedRoutes {
  


  
  
  val index =
          respondWithMediaType(`text/html`) {  // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  <h1>Postgres REST is running</h1>
                </body>
              </html>
            }
          }

  
  val route:Route = {
    
      import JsonProtocol._

    

      path("") {
        getFromFile("index.html")
      } ~
      pathPrefix("js") {
        path(Segment) { file =>
            getFromFile("js/"+file)
        }
      } ~
      pathPrefix("css") {
        path(Segment) { file =>
          getFromFile("client/target/web/sass/main/" + file)
        }
      } ~
      pathPrefix("lib") {
        path(Segment) { file =>
          getFromFile("client/target/scala-2.11/classes/" + file)
        }
      } ~
      path("api" / "v1") {  
        cors {
          options {
            complete(spray.http.StatusCodes.OK)
          } ~
          authenticate(BasicAuth(CustomUserPassAuthenticator, "person-security-realm")) { userProfile =>
            implicit val db = userProfile.db
            generatedRoutes() ~
            path("models") {
              get {
                complete(models ++ views)
              }
            } ~
            pathPrefix("form") {
              path(Segment) { name =>
                get {
                  complete(Forms(name))
                }
              } ~
              pathEnd {
                get {
                  complete(Forms.list)
                }
              }
            }
          }
        }
      }
    
  }


}

