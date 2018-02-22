package ch.wsl.box.rest.routes

import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.{JSONID, JSONMetadata, JSONQuery}
import ch.wsl.box.rest.logic.{FormActions, JSONFormMetadataFactory, JSONMetadataFactory}
import ch.wsl.box.rest.utils.JSONSupport
import ch.wsl.box.shared.utils.CSV
import io.circe.Json
import io.circe.parser.parse
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by andre on 5/15/2017.
  */
case class Form(name:String,lang:String)(implicit db:Database, ec: ExecutionContext, mat:Materializer) extends enablers.CSVDownload {

    import JSONSupport._
    import akka.http.scaladsl.model._
    import akka.http.scaladsl.server.Directives._
    import ch.wsl.box.shared.utils.Formatters._
    import io.circe.generic.auto._
    import ch.wsl.box.shared.utils.JsonUtils._
    import ch.wsl.box.model.shared.EntityKind


    private def actions[T](futForm:Future[JSONMetadata])(f:FormActions => T):Future[T] = for{
      form <- futForm
      formActions = FormActions(form)
    } yield {
      f(formActions)
    }



      val jsonCustomMetadataFactory = JSONFormMetadataFactory()
      val metadata: Future[JSONMetadata] = jsonCustomMetadataFactory.of(name,lang)
      val tabularMetadata = metadata.map{ f =>
        val filteredFields = f.fields.filter(field => f.tabularFields.contains(field.name))
        f.copy(fields = filteredFields)
      }

      def route = pathPrefix("id") {
        path(Segment) { id =>
          get {
            complete(actions(metadata){ fs =>
              fs.getAllById(JSONID.fromString(id)).map{ record =>
                println(record)
                HttpEntity(ContentTypes.`application/json`,record)
              }
            })
          } ~
          put {
            entity(as[Json]) { e =>
              complete {
                actions(metadata){ fs =>
                  for {
                    _ <- fs.updateAll(e)
                    data <- fs.getAllById(e.ID(fs.metadata.keys))
                  } yield data
                }
              }
            }
          } ~
          delete {
            ???
          }
        }
      } ~
      path("kind") {
        get {
          complete{EntityKind.FORM.kind}
        }
      } ~
      path("metadata") {
        get {
          complete {
            metadata
          }
        }
      } ~
      path("children") {
        get {
          complete {
            metadata.flatMap{ f => jsonCustomMetadataFactory.children(f)}
          }
        }
      } ~
      path("keys") {
        get {
          complete {
            metadata.map(f => JSONMetadataFactory.keysOf(f.entity) )
          }
        }
      } ~
      path("ids") {
        post {
          entity(as[JSONQuery]) { query =>
            complete {
              for{
                f <- metadata
                data <- EntityActionsRegistry.tableActions(f.entity).ids(query)
              } yield data
            }
          }
        }
      } ~
      path("count") {
        get {
          complete {
            metadata.map { f =>
              EntityActionsRegistry.tableActions(f.entity).count()
            }
          }
        }
      } ~
      path("list") {
        post {
          entity(as[JSONQuery]) { query =>
            println("list")
            complete(actions(tabularMetadata){ fs =>
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
            complete(actions(tabularMetadata){ fs =>
              fs.csv(query)
            })
          }
        } ~
        respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> s"$name.csv"))) {
          get {
            parameters('q) { q =>
              val query = parse(q).right.get.as[JSONQuery].right.get
              complete(actions(tabularMetadata) { fs =>
                Source.fromFuture(tabularMetadata.map(x => CSV.row(x.fields.map(_.name))))
                  .concat(fs.csv(query))
              })
            }
          }
        }
      } ~
      pathEnd {
          post {
            entity(as[Json]) { e =>
              complete {
                actions(metadata){ fs =>
                  fs.insertAll(e)
                }
              }
            }
          }
      }


}
