package ch.wsl.box.rest.service

import akka.http.scaladsl.server.Directives
import ch.wsl.box.model.shared.{JSONKeys, JSONQuery}
import ch.wsl.box.rest.logic.Forms
import io.circe.Json

import scala.util.{Failure, Success}


/**
  * Created by andre on 5/15/2017.
  */
trait RouteForm {
  def route(name:String) = {

    import JSONSupport._
    import Directives._
    import io.circe.generic.auto._
    import akka.http.scaladsl.server.Directives._

    import akka.http.scaladsl.model._

    pathPrefix(name) {
      pathPrefix("id") {
        path(Segment) { id =>
          get {
            ???
          } ~
            put {
              entity(as[Json]) { e =>
                ???
              }
            } ~
            delete {
              ???
            }
        }
      } ~
        path("schema") {
          get {
            complete {
              ???
            }
          }
        } ~
        path("form") {
          get {
            complete {
              Forms(name)
            }
          }
        } ~
        path("keys") {
          get {
            complete {
              ???
            }
          }
        } ~
        path("count") {
          get {
            complete {
              ???
            }
          }
        } ~
        path("list") {
          post {
            entity(as[JSONQuery]) { query =>
              println("list")
              ???
            }
          }
        } ~
        path("csv") {
          post {
            entity(as[JSONQuery]) { query =>
              println("csv")

              complete(???)
            }
          }
        } ~
        pathEnd {
          get {
            ???
          } ~
            post {
              ???
            }
        }
    }


  }
}
