package ch.wsl.box.rest.routes

import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.utils.{JSONSupport, Timer, UserProfile}
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}
import io.circe.Json
import io.circe.parser.parse
import scribe.Logging
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by andre on 5/15/2017.
  */
case class Form(name:String,lang:String)(implicit up:UserProfile, ec: ExecutionContext, mat:Materializer) extends enablers.CSVDownload with Logging {

    import JSONSupport._
    import akka.http.scaladsl.model._
    import akka.http.scaladsl.server.Directives._
    import ch.wsl.box.shared.utils.Formatters._
    import io.circe.generic.auto._
    import ch.wsl.box.shared.utils.JsonUtils._
    import ch.wsl.box.model.shared.EntityKind
    import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport

    implicit val db = up.db

    private def actions[T](futureForm:Future[JSONMetadata])(f:FormActions => T):Future[T] = for{
      form <- futureForm
      formActions = FormActions(form)
    } yield {
      f(formActions)
    }

    val jsonActions = EntityActionsRegistry().tableActions

    val jsonCustomMetadataFactory = JSONFormMetadataFactory()

    val metadata: Future[JSONMetadata] = jsonCustomMetadataFactory.of(name,lang)

    def tabularMetadata(fields:Option[Seq[String]] = None) = metadata.map{ f =>
      val filteredFields = fields match {
        case Some(fields) => f.fields.filter(field => fields.contains(field.name))
        case None => f.fields.filter(field => f.tabularFields.contains(field.name))
      }
      f.copy(fields = filteredFields)
    }

    def route = pathPrefix("id") {
      path(Segment) { strId =>
        JSONID.fromString(strId) match {
          case Some(id) =>
            get {
              complete(actions(metadata) { fs =>
                fs.getAllById(id).map { record =>
                  logger.info(record.toString)
                  HttpEntity(ContentTypes.`application/json`, record)
                }
              })
            } ~
              put {
                entity(as[Json]) { e =>
                  complete {
                    actions(metadata) { fs =>
                      for {
                        _ <- fs.updateAll(e)
                        data <- fs.getAllById(e.ID(fs.metadata.keys))
                      } yield data
                    }
                  }
                }
              } ~
              delete {
                complete {
                  actions(metadata) { fs =>
                    for {
                      count <- fs.deleteAll(id)
                    } yield JSONCount(count)
                  }
                }
              }
          case None => complete(StatusCodes.BadRequest,s"JSONID $strId not valid")
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
              data <- jsonActions(f.entity).ids(query)
            } yield data
          }
        }
      }
    } ~
    path("count") {
      get {
        complete {
          metadata.map { f =>
            jsonActions(f.entity).count()
          }
        }
      }
    } ~
    path("list") {
      post {
        entity(as[JSONQuery]) { query =>
          logger.info("list")
          complete(actions(tabularMetadata()){ fs =>
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
          logger.info("csv")
          complete{
            for{
              metadata <- tabularMetadata()
            } yield {
              val formActions = FormActions(metadata)
              formActions.csv(query,None)
            }
          }
        }
      } ~
      respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> s"$name.csv"))) {
        get {
          parameters('q, 'fk.?,'fields.?) { (q,fk,fields) =>
            val query = parse(q).right.get.as[JSONQuery].right.get
            val tabMetadata = tabularMetadata(fields.map(_.split(",").toSeq))
            complete{
              for {
                metadata <- tabMetadata
                fkValues <- fk match {
                  case Some(ExportMode.RESOLVE_FK) => Lookup.valuesForEntity(metadata).map(Some(_))
                  case _ => Future.successful(None)
                }
              } yield {

                logger.info(s"fk: ${fkValues.toString.take(50)}...")
                val formActions = FormActions(metadata)

                val headers = metadata.exportFields.map(ef => metadata.fields.find(_.name == ef).map(_.title).getOrElse(ef))

                Source.fromFuture(Future.successful(CSV.writeRow(headers)))
                  .concat(formActions.csv(query,fkValues,_.exportFields))
              }
            }
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
