package ch.wsl.rest.service

import java.io.FileOutputStream
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.language.postfixOps
import akka.actor.Actor
import ch.wsl.rest.domain.ProductionDB
import spray.http.MediaTypes.{ `text/html` }
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{ read, write }
import spray.httpx.Json4sSupport
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.routing.Route
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.authentication.BasicAuth
import spray.routing.authentication.UserPass
import spray.routing.authentication.UserPassAuthenticator
import spray.routing.authentication.UserPassAuthenticator
import spray.routing.directives.AuthMagnet.fromContextAuthenticator
import spray.routing.directives.FieldDefMagnet.apply
import ch.wsl.rest.domain.DBConfig
import ch.wsl.rest.domain.JSONSchema
import ch.wsl.model.Tables
import scala.slick.driver.PostgresDriver.simple._


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MainServiceActor extends Actor with MainService with ProductionDB {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(s4Route)
}



object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}



// this trait defines our service behavior independently from the service actor
trait MainService extends HttpService { this:DBConfig =>
  
  
  //TODO Extend UserProfile class depending on project requirements
  case class UserProfile(name: String)

  def getUserProfile(name: String, password: String): Option[UserProfile] = {
    //TODO Here you should check if this is a valid user on your system and return his profile
    //I'm just creating one a fake one for now on the assumption that only 'bob' exits
    if (name == "bob" && password == "123")
      Some(UserProfile(s"$name"))
    else
      None
  }

  object CustomUserPassAuthenticator extends UserPassAuthenticator[UserProfile] {
    def apply(userPass: Option[UserPass]) = Promise.successful(
      userPass match {
        case Some(UserPass(user, pass)) => {
          getUserProfile(user, pass)
        }
        case _ => None
      }).future
  }

  def modelRoute[M](name:String, table:TableQuery[_ <: Table[M]])(implicit mar:Marshaller[M], unmar: Unmarshaller[M]):Route = { 
    
    
    import Json4sProtocol._
    
    
    pathPrefix(name) {
            path(IntNumber) { i=>
              get {
                complete{ "test:" + i  }
              } ~ 
              post {
                entity(as[M]) { e =>
                  val result = db withSession { implicit s => table.update(e) }
                  complete(e)
                }
              }
            } ~
            path("describe") {
              get {
                complete{ JSONSchema.of(name) }
              }
            } ~
            pathEnd{
              get { ctx =>
                ctx.complete {
                  val result: List[M] = db withSession { implicit s => table.list }
                  result
                }
              } ~
              post { 
                entity(as[M]) { e =>
                  val result = db withSession { implicit s => table.insert(e) }
                  complete(e)
                }
              }
            }
        } 
  }
  
  val index = get { ctx =>
          respondWithMediaType(`text/html`) {  // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  <h1>The <b>S4</b> - <i>Slick Spray Scala Stack</i> is running :-)</h1>
                </body>
              </html>
            }
          }
        }
  
  val s4Route = {
    
      import Json4sProtocol._
    

    
      pathEnd {
        index
      } ~
      authenticate(BasicAuth(CustomUserPassAuthenticator, "person-security-realm")) { userProfile =>
        modelRoute[Tables.CantonRow]("canton", Tables.Canton) 
      } 
    
  }


}

