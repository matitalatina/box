package ch.wsl.box.codegen


import com.typesafe.config.Config
import slick.model.Model
import net.ceedubs.ficus.Ficus._

import scala.util.Try



case class FileAccessGenerator(model:Model,conf:Config) extends slick.codegen.SourceCodeGenerator(model)
  with BoxSourceCodeGenerator
  with slick.codegen.OutputHelpers {


  def file(conf:Config) = {
    val table = conf.getString(
      "table")
    val bytea = conf.getString("bytea")
    val thumbnail = Try(conf.getString("thumbnail")).toOption
    val filename = conf.getString("filename")
    val mime = conf.getString("mime")
    val tableTableQuery = tables.find(_.model.name.table == table).get.TableClass.name
    val tableTableRow = tables.find(_.model.name.table == table).get.EntityType.name

    val thumbnailCode = thumbnail match {
      case Some(t) =>
        s"""
           |        override def injectThumbnail(row: $tableTableRow, file: Array[Byte]) = row.copy(
           |          $t = Some(file)
           |        )
           |        override def extractThumbnail(row: $tableTableRow) = BoxFile(
           |          row.$t,
           |          Some("image/jpeg"),
           |          row.$filename
           |        )
            """.stripMargin
      case None =>
        s"""
           |        override def injectThumbnail(row: $tableTableRow, file: Array[Byte]) = row
           |        override def extractThumbnail(row: $tableTableRow) = BoxFile(
           |          row.$bytea,
           |          row.$mime,
           |          row.$filename
           |        )
            """.stripMargin
    }

    s"""
       |    File("$table.$bytea",$tableTableQuery,new FileHandler[$tableTableRow] {
       |        override def inject(row: $tableTableRow, file: Array[Byte], metadata: FileInfo) = row.copy(
       |          $bytea = Some(file),
       |          $filename = Some(metadata.fileName),
       |          $mime = Some(metadata.contentType.mediaType.toString)
       |        )
       |        override def extract(row: $tableTableRow) = BoxFile(
       |          row.$bytea,
       |          row.$mime,
       |          row.$filename
       |        )
       |        $thumbnailCode
       |    }).route""".stripMargin

  }

  val filesCodes:Seq[String] = conf.as[Seq[Config]]("generator.files").map(file)
  val filesCode:String = filesCodes.nonEmpty match {
    case true => filesCodes.mkString(" ~ ")
    case false => """pathEnd{complete("No files handlers")}"""
  }


  def generate(pkg:String,name:String,modelPackages:String):String =
    s"""package $pkg
       |
       |import akka.http.scaladsl.server.Route
       |import akka.http.scaladsl.server.directives.FileInfo
       |import $modelPackages._
       |import ch.wsl.box.rest.routes.File.{BoxFile, FileHandler}
       |
       |import akka.stream.Materializer
       |import scala.concurrent.ExecutionContext
       |import slick.jdbc.PostgresProfile.api.Database
       |
       |object $name {
       |
       |  import akka.http.scaladsl.server.Directives._
       |
       |  def route(implicit db:Database,materializer:Materializer,ec:ExecutionContext):Route = $filesCode
       |}
     """.stripMargin

  override def writeToFile(folder:String, pkg:String, name:String, fileName:String,modelPackages:String) =
    writeStringToFile(generate(pkg,name,modelPackages),folder,pkg,fileName)



}
