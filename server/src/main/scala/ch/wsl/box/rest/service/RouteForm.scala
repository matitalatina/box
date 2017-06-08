package ch.wsl.box.rest.service

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives
import ch.wsl.box.model.TablesRegistry
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

  def formRoutes(name:String,lang:String)(implicit db:Database) = {

    import JSONSupport._
    import Directives._
    import io.circe.generic.auto._
    import akka.http.scaladsl.server.Directives._

    import akka.http.scaladsl.model._


      val form = Forms(name,lang)
      val tableForm = form.map{ f =>
        val filteredFields = f.fields.filter(field => f.tableFields.contains(field.key))
        f.copy(fields = filteredFields)
      }

      pathPrefix("id") {
        path(Segment) { id =>
          get {
            complete(shaper(form){ fs =>
              fs.extractOne(JSONKeys.fromString(id).query).map{record =>
                HttpEntity(ContentTypes.`text/plain(UTF-8)`,record)
              }
            })
          } ~
            put {
              entity(as[Json]) { e =>
                complete {
                  shaper(form){ fs =>
                    fs.updateAll(e)
                  }
                }
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
        path("subform") {
          get {
            complete {
              form.flatMap{ f => Forms.subforms(f)}
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
        path("keysList") {
          post {
            entity(as[JSONQuery]) { query =>
              complete {
                for{
                  f <- form
                  result <- TablesRegistry.actions(f.table).keyList(query,f.table)
                } yield result
              }
            }
          }
        } ~
        path("count") {
          get {
            complete {
              form.map { f =>
                TablesRegistry.actions(f.table).count()
              }
            }
          }
        } ~
        path("list") {
          post {
            entity(as[JSONQuery]) { query =>
              println("list")
              complete(shaper(tableForm){ fs =>
                fs.extractArray(query).map{arr =>
                  HttpEntity(ContentTypes.`text/plain(UTF-8)`,arr)
                }
              })
            }
          }
        } ~
        path("csv") {
          post {
            entity(as[JSONQuery]) { query =>
              println("csv")
              complete(shaper(tableForm){ fs =>
                fs.csv(query).map{csv =>
                  HttpEntity(ContentTypes.`text/plain(UTF-8)`,csv)
                }
              })
            }
          }
        } ~
        pathEnd {
            post {
              entity(as[Json]) { e =>
                complete {
                  shaper(form){ fs =>
                    fs.insertAll(e)
                  }
                }
              }
            }
        }



  }
}
