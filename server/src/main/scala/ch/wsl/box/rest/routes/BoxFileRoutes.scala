package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import ch.wsl.box.model.FileTables._
import ch.wsl.box.model.boxentities.UIscrTable
import ch.wsl.box.model.boxentities.UIscrTable.UIsrc_row
import ch.wsl.box.rest.routes.File.{BoxFile, FileHandler}
import ch.wsl.box.rest.utils.UserProfile
import ch.wsl.box.rest.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext


object BoxFileRoutes {

  import akka.http.scaladsl.server.Directives._

  def route(implicit up:UserProfile, materializer:Materializer,ec:ExecutionContext):Route = {
    implicit val db = up.db

    File("ui_src.file", UIscrTable.table, new FileHandler[UIsrc_row] {
      override def inject(row: UIsrc_row, file: Array[Byte], metadata: FileInfo) = row.copy(
        file = Some(file),
        name = Some(metadata.fileName),
        mime = Some(metadata.contentType.mediaType.toString)
      )

      override def extract(row: UIsrc_row) = BoxFile(
        row.file,
        row.mime,
        row.name
      )

      override def injectThumbnail(row: UIsrc_row, file: Array[Byte]) = row

      override def extractThumbnail(row: UIsrc_row) = BoxFile(
        row.file,
        row.mime,
        row.name
      )
    }).route
  }
}
     
