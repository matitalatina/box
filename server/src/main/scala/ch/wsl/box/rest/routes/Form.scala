package ch.wsl.box.rest.routes

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import ch.wsl.box.jdbc.FullDatabase
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.utils.{Auth, JSONSupport, Timer, UserProfile, XLSExport, XLSTable}
import io.circe.Json
import io.circe.parser.parse
import scribe.Logging
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.{EntityMetadataFactory, MetadataFactory}
import ch.wsl.box.rest.runtime.Registry

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by andre on 5/15/2017.
  */
case class Form(
                 name:String,
                 lang:String,
                 jsonActions: String => TableActions[Json], //EntityActionsRegistry().tableActions
                 metadataFactory: MetadataFactory, //JSONFormMetadataFactory(),
                 db:Database,
                 kind:String,
                 public: Boolean = false
               )(implicit up:UserProfile, ec: ExecutionContext, mat:Materializer) extends enablers.CSVDownload with Logging {

    import JSONSupport._
    import akka.http.scaladsl.model._
    import akka.http.scaladsl.server.Directives._
    import io.circe.generic.auto._
    import io.circe.syntax._
    import ch.wsl.box.shared.utils.Formatters._ //need to be after circe generic auto or it will be overridden
    import ch.wsl.box.shared.utils.JSONUtils._
    import ch.wsl.box.model.shared.EntityKind
    import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport


    implicit val implicitDB = db
    implicit val boxDb = FullDatabase(db,Auth.adminDB)

    private def actions[T](futureForm:Future[JSONMetadata])(f:FormActions => T):Future[T] = for{
      form <- futureForm
      formActions = FormActions(form,jsonActions,metadataFactory)
    } yield {
      f(formActions)
    }



    val metadata: Future[JSONMetadata] = metadataFactory.of(name,lang)

    def xls:Route = path("xlsx") {
      respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment, Map("filename" -> s"$name.xlsx"))) {
        get {
          parameters('q) { q =>
            val query = parse(q).right.get.as[JSONQuery].right.get
            complete {
              for {
                metadata <- tabularMetadata()
                formActions = FormActions(metadata, jsonActions, metadataFactory)
                fkValues <- Lookup.valuesForEntity(metadata).map(Some(_))
                data <- formActions.list(query, fkValues)
              } yield {
                val table = XLSTable(
                  title = name,
                  header = metadata.exportFields.map(ef => metadata.fields.find(_.name == ef).map(_.title).getOrElse(ef)),
                  rows = data.map(row => metadata.exportFields.map(cell => row.get(cell)))
                )
                val os = new ByteArrayOutputStream()
                XLSExport(table, os)
                os.flush()
                os.close()
                HttpResponse(entity = HttpEntity(MediaTypes.`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, os.toByteArray))
              }
            }
          }
        }
      }
    }

    private def _tabMetadata(fields:Option[Seq[String]] = None,m:JSONMetadata): Seq[JSONField] = {
        fields match {
          case Some(fields) => m.fields.filter(field => fields.contains(field.name))
          case None => m.fields.filter(field => m.tabularFields.contains(field.name))
        }

    }

    def tabularMetadata(fields:Option[Seq[String]] = None) = metadata.flatMap{ m =>
      val filteredFields = m.view match {
        case None => Future.successful(_tabMetadata(fields,m))
        case Some(view) => EntityMetadataFactory.of(view,lang).map{ em =>
          _tabMetadata(Some(fields.getOrElse(m.tabularFields)),em)
        }
      }

      filteredFields.map( ff => m.copy(fields = ff ))

    }

    def privateOnly(r: => Route):Route = {
      if(public) {
        complete(StatusCodes.Unauthorized,"Not authorized to do that action without authentication")
      } else {
        r
      }
    }

    def route = pathPrefix("id") {
      path(Segment) { strId =>
        JSONID.fromString(strId) match {
          case Some(id) =>
            get {
              privateOnly {
                complete(actions(metadata) { fs =>
                  db.run(fs.getById(id).transactionally).map { record =>
                    logger.info(record.toString)
                    HttpEntity(ContentTypes.`application/json`, record.asJson)
                  }
                })
              }
            } ~
              put {
                privateOnly {
                  entity(as[Json]) { e =>
                    complete {
                      actions(metadata) { fs =>
                        for {
                          rowsChanged <- db.run(fs.upsertIfNeeded(Some(id), e).transactionally)
                        } yield rowsChanged
                      }
                    }
                  }
                }
              } ~
              delete {
                privateOnly {
                  complete {
                    actions(metadata) { fs =>
                      for {
                        count <- db.run(fs.delete(id).transactionally)
                      } yield JSONCount(count)
                    }
                  }
                }
              }
          case None => complete(StatusCodes.BadRequest,s"JSONID $strId not valid")
        }
      }
    } ~
    path("kind") {
      get {
        complete{kind}
      }
    } ~
    path("metadata") {
      get {
        complete {
          metadata
        }
      }
    } ~
    path("tabularMetadata") {
      get {
        complete {
          tabularMetadata()
        }
      }
    } ~
    path("schema") {
      get {
        complete {
          metadata.map(new JSONSchemas(Auth.adminDB).of)
        }
      }
    } ~
    path("children") {
      get {
        complete {
          metadata.flatMap{ f => metadataFactory.children(f)}
        }
      }
    } ~
    path("keys") {
      get {
        complete {
          metadata.map(f => EntityMetadataFactory.keysOf(f.entity) )
        }
      }
    } ~
    path("ids") {
      privateOnly {
        post {
          entity(as[JSONQuery]) { query =>
            complete {
              for {
                metadata <- tabularMetadata()
                formActions = FormActions(metadata, jsonActions, metadataFactory)
                data <- db.run(formActions.ids(query))
              } yield data
            }
          }
        }
      }
    } ~
    path("count") {
      get {
        complete {
          metadata.map { f =>
            db.run(jsonActions(f.entity).count())
          }
        }
      }
    } ~
    path("list") {
      post {
        privateOnly {
          entity(as[JSONQuery]) { query =>
            logger.info("list")
            complete(
              for {
                metadata <- tabularMetadata()
                formActions = FormActions(metadata, jsonActions, metadataFactory)
                fkValues <- Lookup.valuesForEntity(metadata).map(Some(_))
                result <- formActions.list(query, fkValues)
              } yield {
                result
              }
            )
          }
        }
      }
    } ~
    xls ~
    path("csv") {
      post {
        privateOnly {
          entity(as[JSONQuery]) { query =>
            logger.info("csv")
            complete {
              for {
                metadata <- tabularMetadata()
              } yield {
                val formActions = FormActions(metadata, jsonActions, metadataFactory)
                formActions.csv(query, None).log("csv")
              }
            }
          }
        }
      } ~
      respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> s"$name.csv"))) {
        get {
          privateOnly {
            parameters('q, 'fk.?, 'fields.?) { (q, fk, fields) =>
              val query = parse(q).right.get.as[JSONQuery].right.get
              val tabMetadata = tabularMetadata(fields.map(_.split(",").toSeq))
              complete {
                for {
                  metadata <- tabMetadata
                  fkValues <- fk match {
                    case Some(ExportMode.RESOLVE_FK) => Lookup.valuesForEntity(metadata).map(Some(_))
                    case _ => Future.successful(None)
                  }
                } yield {

                  logger.info(s"fk: ${fkValues.toString.take(50)}...")
                  val formActions = FormActions(metadata, jsonActions, metadataFactory)

                  val headers = metadata.exportFields.map(ef => metadata.fields.find(_.name == ef).map(_.title).getOrElse(ef))

                  import kantan.csv._
                  import kantan.csv.ops._

                  Source.fromFuture(Future.successful(Seq(headers).asCsv(rfc)))
                    .concat(formActions.csv(query, fkValues, _.exportFields))
                }
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
                db.run(fs.insert(e).transactionally)
              }
            }
          }
        }
    }


}
