package ch.wsl.box.rest.routes

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.model.shared.{JSONCount, JSONData, JSONQuery}
import ch.wsl.box.rest.logic.{DbActions, JSONViewActions, Lookup}
import ch.wsl.box.rest.utils.{JSONSupport, UserProfile}
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}
import io.circe.{Decoder, Encoder}
import io.circe.parser.parse
import scribe.Logging
import slick.lifted.TableQuery
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.EntityMetadataFactory

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
        path("keys") {   //returns key fields names
          get {
            complete{ Seq[String]()} //JSONSchemas.keysOf(name)
          }
        } ~
        path("ids") {   //returns all id values in JSONIDS format filtered according to specified JSONQuery (as body of the post)
          post {
            entity(as[JSONQuery]) { query =>
              complete {
                dbActions.ids(query)
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
              complete(dbActions.find(query))
            }
          }
        } ~
          path("csv") {           //all values in csv format according to JSONQuery
            post {
              entity(as[JSONQuery]) { query =>
                logger.info("csv")
                //Source
                complete(Source.fromPublisher(dbActions.findStreamed(query).mapResult(x => CSV.writeRow(x.values()))).log("csv"))
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

                        Source.fromFuture(Future.successful(
                          CSV.writeRow(metadata.fields.map(_.name))
                        )).concat(Source.fromPublisher(dbActions.findStreamed(query).mapResult{x =>
                          CSV.writeRow(x.values())
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
