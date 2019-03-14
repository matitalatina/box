package ch.wsl.box.rest.metadata

import ch.wsl.box.model.shared.JSONMetadata

import scala.concurrent.Future

trait MetadataFactory{
  def of(name:String, lang:String):Future[JSONMetadata]
  def of(id:Int, lang:String):Future[JSONMetadata]
  def children(form:JSONMetadata):Future[Seq[JSONMetadata]]
  def list: Future[Seq[String]]
}