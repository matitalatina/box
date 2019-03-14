package ch.wsl.box.rest.routes

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`, `Content-Type`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.ByteString
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.jdbc.JdbcConnect
import ch.wsl.box.rest.utils.{JSONSupport, UserProfile}
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}
import io.circe.Json
import io.circe.parser.parse
import scribe.Logging
import ch.wsl.box.rest.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.ExportMetadataFactory

import scala.concurrent.ExecutionContext

object Export extends Logging {

  import ch.wsl.box.shared.utils.JSONUtils._
  import JSONSupport._
  import ch.wsl.box.shared.utils.Formatters._
  import io.circe.generic.auto._


  def csv(function:String,params:Seq[Json],lang:String)(implicit up:UserProfile, ec:ExecutionContext) ={
    implicit val db = up.db

    onSuccess(JdbcConnect.function(function, params,lang)) {
    case None => complete(StatusCodes.BadRequest)
    case Some(fr) =>
      respondWithHeaders(`Content-Disposition`(ContentDispositionTypes.attachment, Map("filename" -> s"$function.csv"))) {
        {
          val csv = CSV.writeAll(Seq(fr.headers) ++ fr.rows.map(_.map(_.string)))
          complete(HttpEntity(ContentTypes.`text/csv(UTF-8)`,ByteString(csv)))
        }
      }
    }
  }


  def route(implicit up:UserProfile, ec:ExecutionContext,mat:Materializer):Route = {
    pathPrefix("list") {
      //      complete(JSONExportMetadataFactory().list)
      path(Segment) { lang =>
        get {
          complete(ExportMetadataFactory().list(lang))
        }
      }
    } ~
      pathPrefix(Segment) { function =>
        pathPrefix("def") {
          //      complete(JSONExportMetadataFactory().list)
          path(Segment) { lang =>
            get {
              complete(ExportMetadataFactory().defOf(function, lang))
            }
          }
        }
      }~
//      pathPrefix("") {
        pathPrefix(Segment) { function =>
          pathPrefix("metadata") {
            path(Segment) { lang =>
              get {
                complete(ExportMetadataFactory().of(function, lang))
              }
            }
          } ~
          path(Segment) { lang =>
            get {
              parameters('q) { q =>
                val params = parse(q).right.get.as[Seq[Json]].right.get
                csv(function, params, lang)
              }
            } ~
              post {
                entity(as[Seq[Json]]) { params =>
                  csv(function, params, lang)
                }
              }
          }
        }
//      }
  }
}
