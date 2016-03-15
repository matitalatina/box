package ch.wsl.rest.service

import ch.wsl.rest.domain.Forms
import ch.wsl.rest.service.Auth.CustomUserPassAuthenticator
import spray.routing.PathMatchers.Segment
import spray.routing._
import spray.routing.authentication.BasicAuth
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by andreaminetti on 15/03/16.
  */
trait RouteRoot extends HttpService with CORSSupport with RouteTable with RouteView with RouteUI with GeneratedRoutes {
  /**
    * Base of all HTTP calls
    */
  val route:Route = {

    import JsonProtocol._

    //Serving UI
    clientFiles ~
      //Serving REST-API
      pathPrefix("api" / "v1") {
        //version 1
        cors {
          // enable cross domain usage
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
