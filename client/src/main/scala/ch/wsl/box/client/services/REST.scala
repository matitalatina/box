package ch.wsl.box.client.services

import ch.wsl.box.client.services.REST.get
import ch.wsl.box.model.shared._
import io.circe.Json
import org.scalajs.dom.File

import scala.concurrent.Future

/**
  * Created by andre on 4/24/2017.
  */
object REST {


  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.Formatters._
  import scalajs.concurrent.JSExecutionContext.Implicits.queue
  import ch.wsl.box.model.shared.EntityKind._

  private def client = HttpClient("/api/v1")


  def entities(kind:String):Future[Seq[String]] = client.get[Seq[String]](s"/${EntityKind(kind).plural}")

  //for entities and forms
  def specificKind(kind:String, lang:String, entity:String):Future[String] = client.get[String](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/kind")
  def list(kind:String, lang:String, entity:String, limit:Int): Future[Seq[Json]] = client.post[JSONQuery,JSONData[Json]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/list",JSONQuery.empty.limit(limit)).map(_.data)
  def list(kind:String, lang:String, entity:String, query:JSONQuery): Future[Seq[Json]] = client.post[JSONQuery,JSONData[Json]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/list",query).map(_.data)
  def csv(kind:String, lang:String, entity:String, q:JSONQuery): Future[Seq[Seq[String]]] = client.post[JSONQuery,String](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/csv",q).map{ result =>
    result.split("\r\n").toSeq.map{x => x.substring(1,x.length-1).split("\",\"").toSeq}
  }
  def count(kind:String, lang:String, entity:String): Future[Int] = client.get[Int](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/count")
  def keys(kind:String, lang:String, entity:String): Future[Seq[String]] = client.get[Seq[String]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/keys")
  def ids(kind:String, lang:String, entity:String, q:JSONQuery): Future[IDs] = client.post[JSONQuery,IDs](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/ids",q)
//  def schema(kind:String, lang:String, entity:String): Future[JSONSchema] = client.get[JSONSchema](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/schema")
  def metadata(kind:String, lang:String, entity:String): Future[JSONMetadata] = client.get[JSONMetadata](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/metadata")

  //only for forms
  def children(entity:String, lang:String): Future[Seq[JSONMetadata]] = client.get[Seq[JSONMetadata]](s"/form/$lang/$entity/children")

  //only for entities
  def get(kind:String, lang:String, entity:String, id:JSONID):Future[Json] = client.get[Json](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}")
  def update(kind:String, lang:String, entity:String, id:JSONID, data:Json):Future[Json] = client.put[Json,Json](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}",data)
  def insert(kind:String, lang:String, entity:String, data:Json): Future[Json] = client.post[Json,Json](s"/${EntityKind(kind).entityOrForm}/$lang/$entity",data)
  def delete(kind:String, lang:String, entity:String, id:JSONID):Future[JSONCount] = client.delete[JSONCount](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}")

  //files
  def sendFile(file:File, id:JSONID, entity:String) = client.sendFile[Int](s"/file/$entity/${id.asString}",file)
//  def getFile(entity:String, id:String) = client.getFile(s"/file/$entity/$id")
//  s"/api/v1/file/${entity}.${field.file.get.file}/${id.get}"

  //other utils
  def loginCheck() = client.get[String]("/checkLogin")
  def labels(lang:String):Future[Map[String,String]] = client.get[Map[String,String]](s"/labels/$lang")
  def conf():Future[Map[String,String]] = client.get[Map[String,String]](s"/conf")
  

}
