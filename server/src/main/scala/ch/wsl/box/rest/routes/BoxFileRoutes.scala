package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import ch.wsl.box.model.boxentities.BoxUIsrcTable
import ch.wsl.box.model.boxentities.BoxUIsrcTable.BoxUIsrc_row
import ch.wsl.box.rest.routes.File.{BoxFile, FileHandler}
import ch.wsl.box.rest.utils.UserProfile
import ch.wsl.box.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext


object BoxFileRoutes {

  import akka.http.scaladsl.server.Directives._

  def route(implicit up:UserProfile, materializer:Materializer,ec:ExecutionContext):Route = {
    implicit val db = up.db

    File("ui_src.file", BoxUIsrcTable.BoxUIsrcTable, new FileHandler[BoxUIsrc_row] {
      override def inject(row: BoxUIsrc_row, file: Array[Byte], metadata: FileInfo) = row.copy(
        file = Some(file),
        name = Some(metadata.fileName),
        mime = Some(metadata.contentType.mediaType.toString)
      )

      override def extract(row: BoxUIsrc_row) = BoxFile(
        row.file,
        row.mime,
        row.name
      )

      override def injectThumbnail(row: BoxUIsrc_row, file: Array[Byte]) = row

      override def extractThumbnail(row: BoxUIsrc_row) = BoxFile(
        row.file,
        row.mime,
        row.name
      )
    }).route
  }
}
     
