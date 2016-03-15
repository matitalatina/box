package ch.wsl.rest.service

import spray.routing._

/**
  * Created by andreaminetti on 15/03/16.
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
