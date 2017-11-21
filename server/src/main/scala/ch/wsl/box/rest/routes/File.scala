package ch.wsl.box.rest.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.Directives.{complete, fileUpload, path, pathEnd, pathPrefix, post}
import akka.stream.Materializer
import ch.wsl.box.rest.model.DBFile
import ch.wsl.box.rest.utils.Auth.UserProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object File {
  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._
  import io.circe.generic.auto._

  def apply(userProfile: UserProfile)(implicit ec:ExecutionContext,materializer:Materializer):Route = {
    pathPrefix("file") {
      pathEnd {
        post {
          fileUpload("file") { case (metadata, byteSource) =>
            val result = byteSource.runFold(Seq[Byte]()) { (acc, n) => acc ++ n.toSeq }.map(_.toArray).flatMap { bytea =>
              val row = DBFile.Row(name = metadata.fileName, file = bytea, mime = metadata.contentType.mediaType.toString(), insert_by = userProfile.name)
              userProfile.box.run {
                DBFile.table returning DBFile.table.map(_.file_id) += row
              }.map(f => f)
            }
            complete(result)
          }
        }
      }~
        path(Segment) { file_id =>
          complete {
            userProfile.box.run {
              DBFile.table.filter(_.file_id === file_id.toInt).result
            }.map {
              _.headOption.map { result =>
                val contentType = ContentType.parse(result.mime).right.getOrElse(ContentTypes.`application/octet-stream`)
                val entity = HttpEntity(contentType, result.file)
                val contentDistribution:HttpHeader = headers.`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> result.name,"size" -> result.file.length.toString))
                HttpResponse(entity = entity,headers = scala.collection.immutable.Seq(contentDistribution))
              }
            }
          }
        }
    }
  }
}
