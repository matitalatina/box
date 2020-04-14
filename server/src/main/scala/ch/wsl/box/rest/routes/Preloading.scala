package ch.wsl.box.rest.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.directives.CachingDirectives._

object Preloading {
  import Directives._

  val route:Route =
    path("status") {
      cachingProhibited {
        complete("BOOTING")
      }
    } ~
    path("") {
      redirect("/preloading",StatusCodes.TemporaryRedirect)
    } ~
    path("preloading") {
      getFromResource("preloading.html")
    }
}
