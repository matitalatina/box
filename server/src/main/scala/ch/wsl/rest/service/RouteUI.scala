package ch.wsl.rest.service

import spray.routing._

/**
  *
  * Simple route to serve UI-client files
  * Statically serves files
  *
  */
trait RouteUI extends HttpService{

  val clientFiles: Route =
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
