package ch.wsl.box.rest.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

object Preloading {
  import Directives._

  val route:Route =
    path("status") {
      get {
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
