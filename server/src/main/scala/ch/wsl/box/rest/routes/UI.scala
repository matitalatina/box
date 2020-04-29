package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.{Directives, Route}

import akka.http.scaladsl.server.directives.ContentTypeResolver.Default

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
      getFromResource("index.html")
    } ~
    pathPrefix("webjars") {
      WebJarsSupport.webJars
    } ~
    pathPrefix("js") {
      path(Segment) { file =>
        getFromResource("js/"+file)
      }
    } ~
    pathPrefix("webjars") {
      WebJarsSupport.webJars
    }
}
