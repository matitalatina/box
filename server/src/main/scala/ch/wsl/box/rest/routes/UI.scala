package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.{Directives, Route}

/**
  *
  * Simple route to serve UI-client files
  * Statically serves files
  *
  */
object UI {

  import Directives._

  val clientFiles:Route =
    path("") {
      get {
        getFromFile("index.html")
      }
    } ~
    path("test") {
      get {
        getFromFile("test.html")
      }
    } ~
    pathPrefix("js") {
      path(Segment) { file =>
        getFromFile("client/target/scala-2.11/"+file)
      }
    } ~
    pathPrefix("webjars") {
      WebJarsSupport.webJars
    }
}
