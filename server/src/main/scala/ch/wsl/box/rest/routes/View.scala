package ch.wsl.box.rest.routes

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.{JSONCount, JSONData, JSONQuery}
import ch.wsl.box.rest.logic.{DbActions, JSONMetadataFactory, Lookup}
import ch.wsl.box.rest.utils.JSONSupport
import ch.wsl.box.shared.utils.CSV
import io.circe.parser.parse
import scribe.Logging
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by andreaminetti on 16/02/16.
 */
object View {

  var views = Set[String]()

}

case class View[T <: slick.jdbc.PostgresProfile.api.Table[M],M <: Product](name:String, table:TableQuery[T])(implicit
                                                                                                         mat:Materializer,
                                                                                                         unmarshaller: FromRequestUnmarshaller[M],
                                                                                                         marshaller:ToResponseMarshaller[M],
                                                                                                         seqmarshaller: ToResponseMarshaller[Seq[M]],
                                                                                                         jsonmarshaller:ToResponseMarshaller[JSONData[M]],
                                                                                                         db:Database,
                                                                                                         ec: ExecutionContext
                                                                                              ) extends enablers.CSVDownload with Logging {

    View.views = Set(name) ++ View.views

    import Directives._
    import JSONSupport._
  import JSONData._
  import io.circe.generic.auto._
    import ch.wsl.box.shared.utils.Formatters._
    import ch.wsl.box.model.shared.EntityKind
    import io.circe.syntax._

    val helper = new DbActions[T,M](table)

    def route = pathPrefix(name) {
        logger.info(s"view with name: $name")

        path("kind") {
          get {
            complete{EntityKind.VIEW.kind}
          }
        } ~
        path("metadata") {
          get {
            complete{ JSONMetadataFactory.of(name, "en") }
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
                EntityActionsRegistry().viewActions(name).ids(query)
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
              complete(helper.find(query))
            }
          }
        } ~
          path("csv") {           //all values in csv format according to JSONQuery
            post {
              entity(as[JSONQuery]) { query =>
                logger.info("csv")
                Source
                complete(Source.fromPublisher(helper.findStreamed(query)))
              }
            } ~
              respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> s"$name.csv"))) {
                get {
                  parameters('q,'lang.?) { (q, lang) =>
                    val query = parse(q).right.get.as[JSONQuery].right.get
                    complete {
                      for {
                        metadata <- JSONMetadataFactory.of(name, lang.getOrElse("en"))
                        fkValues <- lang match {
                          case None => Future.successful(None)
                          case Some(_) => Lookup.valuesForEntity(metadata).map(Some(_))
                        }
                      } yield {

                        val lookup = Lookup.valueExtractor(fkValues,metadata) _

                        Source.fromFuture(Future.successful(
                          CSV.row(metadata.fields.map(_.name))
                        )).concat(Source.fromPublisher(helper.findStreamed(query)).map{x =>

                          val row = metadata.fields.map(_.name).zip(x.values()).map{ case (field,v) => lookup(field,v)}

                          CSV.row(row)
                        })
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
