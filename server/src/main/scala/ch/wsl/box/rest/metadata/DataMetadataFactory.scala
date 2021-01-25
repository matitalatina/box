package ch.wsl.box.rest.metadata

import ch.wsl.box.model.shared.{ExportDef, JSONMetadata}

import scala.concurrent.Future

trait DataMetadataFactory {
  def list: Future[Seq[String]]
  def list(lang:String): Future[Seq[ExportDef]]
  def defOf(function:String, lang:String): Future[ExportDef]
  def of(schema:String, function:String, lang:String):Future[JSONMetadata]
}
