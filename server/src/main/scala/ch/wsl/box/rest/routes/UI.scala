package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import boxInfo.BoxBuildInfo
import ch.wsl.box.rest.routes.enablers.twirl.Implicits._

/**
  *
  * Simple route to serve UI-client files
  * Statically serves files
  *
  */
object UI {

  import Directives._

  val clientFiles:Route =
    pathSingleSlash {
      get {
        complete {
          ch.wsl.box.templates.html.index.render(BoxBuildInfo.version)
        }
      }
    } ~
    pathPrefix("assets") {
      WebJarsSupport.webJars
    }
}
