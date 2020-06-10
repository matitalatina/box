package ch.wsl.box.rest.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import scribe.Logging

object Preloading extends Logging {
  import Directives._

  val route:Route =
    path("status") {
      get {
        complete("BOOTING")
      }
    } ~
    path("") {
      extractRequestContext { ctx =>
        logger.info(ctx.request.toString())
        redirect(ctx.request.uri.toString() + "preloading", StatusCodes.TemporaryRedirect)
      }
    } ~
    path("preloading") {
      getFromResource("preloading.html")
    }
}
