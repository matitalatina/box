package ch.wsl.box.rest.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.Directives.{complete, fileUpload, path, pathEnd, pathPrefix, post}
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.JSONID
import ch.wsl.box.rest.logic.DbActions
import ch.wsl.box.rest.routes.File.FileHandler
import io.circe.Decoder
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}



object File{
  case class BoxFile(file:Option[Array[Byte]], mime: Option[String], name:Option[String])

  trait FileHandler[M <: Product]{
    def inject(obj:M, file:Array[Byte], metadata:FileInfo):M
    def extract(obj:M):BoxFile
  }
}

case class File[T <: slick.jdbc.PostgresProfile.api.Table[M],M <: Product](field:String, table: TableQuery[T], handler: FileHandler[M])(implicit ec:ExecutionContext, materializer:Materializer, db:Database) {
  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._
  import io.circe.generic.auto._


  val utils = new DbActions[T,M](table)

  def upload(id:JSONID)(metadata:FileInfo, byteSource:Source[ByteString, Any]) = {
    for{
      bytea <- byteSource.runReduce[ByteString]{ (x,y) =>  x ++ y }.map(_.toArray)
      row <- utils.getById(id)
      rowWithFile = handler.inject(row.get,bytea,metadata)
      result <- utils.updateById(id,rowWithFile)
    } yield result
  }


  def route:Route = {
    pathPrefix(field) {
      pathPrefix("generate-thumbnail") { // /api/v1/file/document.b_document/generate-thumbnail/document_id::35/document.b_thumbnail
        path(Segment) { originField => //
          path(Segment) { idstr =>
            complete("")
          }
        }
      } ~
      path(Segment) { idstr =>
        println(s"Parsing File'JSONID: $idstr")
        val id = JSONID.fromString(idstr)

        post {
          fileUpload("file") { case (metadata, byteSource) =>
            val result = upload(id)(metadata, byteSource)
            complete(result)
          }
        } ~
          get {
            complete {
              utils.getById(id).map {
                _.headOption.map { result =>
                  val f = handler.extract(result)

                  val contentType = f.mime.flatMap{ mime =>
                    ContentType.parse(mime).right.toOption
                  }.getOrElse(ContentTypes.`application/octet-stream`)
                  val file = f.file.getOrElse(Array())
                  val name = f.name.getOrElse("noname")

                  val entity = HttpEntity(contentType,file)
                  val contentDistribution: HttpHeader = headers.`Content-Disposition`(ContentDispositionTypes.attachment, Map("filename" -> name, "size" -> file.length.toString))
                  HttpResponse(entity = entity, headers = scala.collection.immutable.Seq(contentDistribution))
                }
              }
            }
          }
      }
    }
  }
}
