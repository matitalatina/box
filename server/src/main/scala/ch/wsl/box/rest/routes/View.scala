package ch.wsl.box.rest.routes

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.{JSONCount, JSONQuery, JSONData}
import ch.wsl.box.rest.logic.{DbActions, JSONMetadataFactory}
import ch.wsl.box.rest.utils.JSONSupport
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by andreaminetti on 16/02/16.
 */
object View {

  var views = Set[String]()

  def apply[T <: slick.jdbc.PostgresProfile.api.Table[M],M <: Product](name:String, table:TableQuery[T])(implicit
                                                                                                         mat:Materializer,
                                                                                                         unmarshaller: FromRequestUnmarshaller[M],
                                                                                                         marshaller:ToResponseMarshaller[M],
                                                                                                         seqmarshaller: ToResponseMarshaller[Seq[M]],
                                                                                                         jsonmarshaller:ToResponseMarshaller[JSONData[M]],
                                                                                                         db:Database,
                                                                                                         ec: ExecutionContext
                                                                                              ):Route = {

    views = Set(name) ++ views

    import Directives._
    import JSONSupport._
    import io.circe.generic.auto._
    import ch.wsl.box.shared.utils.Formatters._
    import ch.wsl.box.model.shared.EntityKind

    val helper = new DbActions[T,M](table)

    pathPrefix(name) {
        println(s"view with name: $name")

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
                EntityActionsRegistry.viewActions(name).ids(query)
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
              println("list")
              complete(helper.find(query))
            }
          }
        } ~
        path("csv") {
          post {
            entity(as[JSONQuery]) { query =>
              println("csv")

              complete(helper.find(query).map(x => HttpEntity(ContentTypes.`text/plain(UTF-8)`,x.csv)))
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
}
