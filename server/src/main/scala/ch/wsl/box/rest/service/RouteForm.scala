package ch.wsl.box.rest.service

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives
import ch.wsl.box.model.shared.{JSONForm, JSONKeys, JSONQuery}
import ch.wsl.box.rest.logic.{FormShaper, Forms, JSONSchemas}
import io.circe.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.PostgresDriver.api._

/**
  * Created by andre on 5/15/2017.
  */
trait RouteForm {

  def shaper[T](futForm:Future[JSONForm])(f:FormShaper => T)(implicit db:Database):Future[T] = for{
    form <- futForm
    formShaper = FormShaper(form)
  } yield {
    f(formShaper)
  }

  def formRoutes(name:String)(implicit db:Database) = {

    import JSONSupport._
    import Directives._
    import io.circe.generic.auto._
    import akka.http.scaladsl.server.Directives._

    import akka.http.scaladsl.model._


      val form = Forms(name)

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
        path("identity") {
          get {
            complete(name)
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
              form
            }
          }
        } ~
        path("keys") {
          get {
            complete {
              form.map(f => JSONSchemas.keysOf(f.table) )
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
              complete(shaper(form){ fs =>
                fs.csv(query).map{csv =>
                  HttpEntity(ContentTypes.`text/plain(UTF-8)`,csv)
                }
              })
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
