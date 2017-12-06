package ch.wsl.box.rest.routes

import ch.wsl.box.model.TablesRegistry
import ch.wsl.box.model.shared.{JSONKeys, JSONMetadata, JSONQuery}
import ch.wsl.box.rest.logic.{FormShaper, JSONFormMetadata, JSONSchemas}
import ch.wsl.box.rest.utils.JSONSupport
import io.circe.Json
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by andre on 5/15/2017.
  */
object Form {

  private def shaper[T](futForm:Future[JSONMetadata])(f:FormShaper => T)(implicit db:Database, ec: ExecutionContext):Future[T] = for{
    form <- futForm
    formShaper = FormShaper(form)
  } yield {
    f(formShaper)
  }

  def apply(name:String,lang:String)(implicit db:Database, ec: ExecutionContext) = {

    import JSONSupport._
    import akka.http.scaladsl.model._
    import akka.http.scaladsl.server.Directives._
    import ch.wsl.box.shared.utils.Formatters._
    import io.circe.generic.auto._
    import ch.wsl.box.shared.utils.JsonUtils._



      val jsonFormMetadata = JSONFormMetadata()
      val form = jsonFormMetadata.get(name,lang)
      val tableForm = form.map{ f =>
        val filteredFields = f.fields.filter(field => f.entityFields.contains(field.key))
        f.copy(fields = filteredFields)
      }

      pathPrefix("id") {
        path(Segment) { id =>
          get {
            complete(shaper(form){ fs =>
              fs.getAllById(JSONKeys.fromString(id)).map{record =>
                println(record)
                HttpEntity(ContentTypes.`application/json`,record)
              }
            })
          } ~
            put {
              entity(as[Json]) { e =>
                complete {
                  shaper(form){ fs =>
                    for {
                      _ <- fs.updateAll(e)
                      result <- fs.getAllById(e.keys(fs.form.keys))
                    } yield result
                  }
                }
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
        path("metadata") {
          get {
            complete {
              form
            }
          }
        } ~
        path("subform") {
          get {
            complete {
              form.flatMap{ f => jsonFormMetadata.subforms(f)}
            }
          }
        } ~
        path("keys") {
          get {
            complete {
              form.map(f => JSONSchemas.keysOf(f.entity) )
            }
          }
        } ~
        path("keysList") {
          post {
            entity(as[JSONQuery]) { query =>
              complete {
                for{
                  f <- form
                  result <- TablesRegistry.actions(f.entity).keyList(query,f.entity)
                } yield result
              }
            }
          }
        } ~
        path("count") {
          get {
            complete {
              form.map { f =>
                TablesRegistry.actions(f.entity).count()
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
