package ch.wsl.box.rest.service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  *
  * Simple route to serve UI-client files
  * Statically serves files
  *
  */
trait RouteUI {

  val clientFiles:Route =
    path("") {
      get {
        getFromFile("index.html")
      }
    } ~
      pathPrefix("js") {
        path(Segment) { file =>
          getFromFile("client/target/scala-2.11/"+file)
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
      }
}
