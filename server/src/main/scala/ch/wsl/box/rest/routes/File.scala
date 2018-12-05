package ch.wsl.box.rest.routes

import java.io.ByteArrayInputStream

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.Directives.{complete, fileUpload, get, path, pathEnd, pathPrefix, post}
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.JSONID
import ch.wsl.box.rest.logic.DbActions
import ch.wsl.box.rest.routes.File.FileHandler
import io.circe.Decoder
import nz.co.rossphillips.thumbnailer.Thumbnailer
import nz.co.rossphillips.thumbnailer.thumbnailers.{DOCXThumbnailer, ImageThumbnailer, PDFThumbnailer, TextThumbnailer}
import scribe.Logging
import ch.wsl.box.rest.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try



object File{
  case class BoxFile(file:Option[Array[Byte]], mime: Option[String], name:Option[String])

  trait FileHandler[M <: Product]{
    def inject(obj:M, file:Array[Byte], metadata:FileInfo):M
    def extract(obj:M):BoxFile
    def injectThumbnail(obj:M, file:Array[Byte]):M
    def extractThumbnail(obj:M):BoxFile
  }

  def completeFile(f:BoxFile) =
    complete {
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

case class File[T <: ch.wsl.box.rest.jdbc.PostgresProfile.api.Table[M],M <: Product](field:String, table: TableQuery[T], handler: FileHandler[M])(implicit ec:ExecutionContext, materializer:Materializer, db:Database) extends Logging {
  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._
  import io.circe.generic.auto._


  val dbActions = new DbActions[T,M](table)

  def createThumbnail(file:Array[Byte],contentType:String):Option[Array[Byte]] = Try{
    val thumbnailer = new Thumbnailer(new PDFThumbnailer, new TextThumbnailer, new ImageThumbnailer, new DOCXThumbnailer)
    thumbnailer.setSize(450,300)
    thumbnailer.generateThumbnail(new ByteArrayInputStream(file),contentType)
  }.toOption

  def upload(id:JSONID)(metadata:FileInfo, byteSource:Source[ByteString, Any]) = {
    for{
      bytea <- byteSource.runReduce[ByteString]{ (x,y) =>  x ++ y }.map(_.toArray)
      row <- dbActions.getById(id)
      rowWithFile = handler.inject(row.get,bytea,metadata)
      rowWithFileAndThumb <- Future{
        createThumbnail(bytea,metadata.contentType.mediaType.toString) match {
          case Some(thumbnail) => handler.injectThumbnail(rowWithFile,thumbnail)
          case None => rowWithFile
        }

      }
      result <- dbActions.updateById(id,rowWithFileAndThumb)
    } yield result
  }


  def route:Route = {
    pathPrefix(field) {

      pathPrefix(Segment) { idstr =>
        logger.info(s"Parsing File'JSONID: $idstr")
        JSONID.fromString(idstr) match {
          case Some(id) => post {
            fileUpload("file") { case (metadata, byteSource) =>
              val result = upload(id)(metadata, byteSource)
              complete(result)
            }
          } ~
            path("thumb") {
              get {
                onSuccess(dbActions.getById(id)) { result =>
                  val f = handler.extractThumbnail(result.head)
                  if(f.file.isEmpty) {
                    val originalFile = handler.extract(result.head)
                    val thumbnailFile = for{
                      file <- originalFile.file
                      mime <- originalFile.mime
                      thumb <- createThumbnail(file,mime)
                    } yield {
                      val row = handler.injectThumbnail(result.head,thumb)
                      dbActions.updateById(id,row)
                      handler.extractThumbnail(row)
                    }
                    thumbnailFile match {
                      case Some(f) => File.completeFile(f)
                      case None => complete(StatusCodes.NotFound)
                    }

                  } else {
                    File.completeFile(f)
                  }
                }
              }
            } ~
            pathEnd {
              get {
                onSuccess(dbActions.getById(id)) { result =>
                  val f = handler.extract(result.head)
                  File.completeFile(f)
                }
              }
            }
          case None => complete(StatusCodes.BadRequest,s"JSONID $idstr not valid")
        }


      }
    }
  }
}
