package ch.wsl.box.rest.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import ch.wsl.box.rest.logic.Forms
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by andreaminetti on 15/03/16.
  */
trait RouteRoot extends RouteTable with RouteView with RouteUI with GeneratedRoutes {

  implicit val materializer:Materializer

  /**
    * Base of all HTTP calls
    */
  val route:Route = {

    import CirceSupport._
    import Directives._
    import ch.megard.akka.http.cors.CorsDirectives._
    import io.circe.generic.auto._
    import ch.wsl.box.rest.service.Auth.PostgresAuthenticator._


    //Serving UI
    clientFiles ~
      //Serving REST-API
      pathPrefix("api" / "v1") {
        //version 1
        cors() {
          // enable cross domain usage
          options {
            complete(StatusCodes.OK)
          } ~
            postgresBasicAuth { userProfile =>
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
