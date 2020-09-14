package ch.wsl.box.rest.routes

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import ch.wsl.box.model.shared.{JSONCount, JSONData, JSONQuery}
import ch.wsl.box.rest.logic.{DbActions, JSONTableActions, JSONViewActions, Lookup}
import ch.wsl.box.rest.utils.{JSONSupport, UserProfile}
import io.circe.{Decoder, Encoder}
import io.circe.parser.parse
import scribe.Logging
import slick.lifted.TableQuery
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.EntityMetadataFactory
import ch.wsl.box.rest.services.{XLSExport, XLSTable}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by andreaminetti on 16/02/16.
 */
object View {

  var views = Set[String]()

}

case class View[T <: ch.wsl.box.jdbc.PostgresProfile.api.Table[M],M <: Product](name:String, table:TableQuery[T], lang:String="en")
                                                    (implicit
                                                     enc: Encoder[M],
                                                     dec:Decoder[M],
                                                     mat:Materializer,
                                                     up:UserProfile,
                                                     ec: ExecutionContext) extends enablers.CSVDownload with Logging {




    View.views = Set(name) ++ View.views

  import JSONSupport._
  import akka.http.scaladsl.model._
  import akka.http.scaladsl.server.Directives._
  import ch.wsl.box.shared.utils.Formatters._
  import io.circe.generic.auto._
  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JSONUtils._
  import ch.wsl.box.model.shared.EntityKind
  import JSONData._

    implicit val db  = up.db

  val dbActions = new DbActions[T,M](table)
  val jsonActions = JSONTableActions[T,M](table)


  def xls:Route = path("xlsx") {
    respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment, Map("filename" -> s"$name.xlsx"))) {
      get {
        parameters('q) { q =>
          val query = parse(q).right.get.as[JSONQuery].right.get
          complete {
            for {
              metadata <- EntityMetadataFactory.of(name, lang)
              fkValues <- Lookup.valuesForEntity(metadata).map(Some(_))
              data <- db.run(jsonActions.find(query))
            } yield {
              val table = XLSTable(
                title = name,
                header = metadata.fields.map(_.name),
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

    def route = pathPrefix(name) {
        logger.info(s"view with name: $name")

        pathPrefix("lookup") {
          pathPrefix(Segment) { textProperty =>
            path(Segment) { valueProperty =>
              post{
                entity(as[JSONQuery]){ query =>
                  complete {
                    Lookup.values(name, valueProperty, textProperty, query)
                  }
                }
              }
            }
          }
        } ~
        path("kind") {
          get {
            complete{EntityKind.VIEW.kind}
          }
        } ~
        path("metadata") {
          get {
            complete{ EntityMetadataFactory.of(name, lang) }
          }
        } ~
        path("tabularMetadata") {
            get {
              complete{ EntityMetadataFactory.of(name, lang) }
            }
        } ~
        path("keys") {   //returns key fields names
          get {
            complete{ Seq[String]()} //JSONSchemas.keysOf(name)
          }
        } ~
        path("ids") {   //returns all id values in JSONIDS format filtered according to specified JSONQuery (as body of the post)
          post {
            entity(as[JSONQuery]) { query =>
              complete {
                db.run(dbActions.ids(query))
//                EntityActionsRegistry().viewActions(name).map(_.ids(query))
              }
            }
          }
        } ~
        path("count") {
          get { ctx =>

            val nr = db.run { table.length.result }.map{r =>
              JSONCount(r)
            }
            ctx.complete{ nr }
          }
        } ~
        path("list") {
          post {
            entity(as[JSONQuery]) { query =>
              logger.info("list")
              complete(db.run(dbActions.find(query)))
            }
          }
        } ~
      xls ~
          path("csv") {           //all values in csv format according to JSONQuery
            post {
              entity(as[JSONQuery]) { query =>
                logger.info("csv")
                //Source
                import kantan.csv._
                import kantan.csv.ops._
                complete(Source.fromPublisher(dbActions.findStreamed(query).mapResult(x => Seq(x.values()).asCsv(rfc))).log("csv"))
              }
            } ~
              respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> s"$name.csv"))) {
                get {
                  parameters('q,'lang.?) { (q, lang) =>
                    val query = parse(q).right.get.as[JSONQuery].right.get
                    complete {
                      for {
                        metadata <- EntityMetadataFactory.of(name, lang.getOrElse("en"))
                        fkValues <- lang match {
                          case None => Future.successful(None)
                          case Some(_) => Lookup.valuesForEntity(metadata).map(Some(_))
                        }
                      } yield {

                        val lookup = Lookup.valueExtractor(fkValues,metadata) _

                        import kantan.csv._
                        import kantan.csv.ops._

                        Source.fromFuture(Future.successful(
                          Seq(metadata.fields.map(_.name)).asCsv(rfc)
                        )).concat(Source.fromPublisher(dbActions.findStreamed(query).mapResult{x =>
                          Seq(x.values()).asCsv(rfc)
                        }))
                      }
                    }
                  }
                }
              }
          } ~
        pathEnd{
          get { ctx =>
            ctx.complete {
              val q = table.take(50)
              val data: Future[Seq[M]] = db.run{ q.result }
              data
            }

          }
        }
    }

}
