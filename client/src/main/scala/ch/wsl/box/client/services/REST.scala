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


  private def client = HttpClient("/api/v1")

  def sendFile(file:File, keys:JSONKeys, entity:String) = client.sendFile[Int](s"/file/$entity/${keys.asString}",file)
  def entities(kind:String):Future[Seq[String]] = kind match {
      case "entity" => client.get[Seq[String]](s"/entities")
      case _ => client.get[Seq[String]](s"/${kind}s")   //for table and view
  }
  def list(kind:String, lang:String, entity:String, limit:Int): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$kind/$lang/$entity/list",JSONQuery.limit(limit)).map(_.data)
  def list(kind:String, lang:String, entity:String, query:JSONQuery): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$kind/$lang/$entity/list",query).map(_.data)
  def csv(kind:String, lang:String, entity:String, q:JSONQuery): Future[Seq[Seq[String]]] = client.post[JSONQuery,String](s"/$kind/$lang/$entity/csv",q).map{ result =>
    result.split("\r\n").toSeq.map{x => x.substring(1,x.length-1).split("\",\"").toSeq}
  }
  def keys(kind:String, lang:String, entity:String): Future[Seq[String]] = client.get[Seq[String]](s"/$kind/$lang/$entity/keys")
  def keysList(kind:String, lang:String, entity:String, q:JSONQuery): Future[KeyList] = client.post[JSONQuery,KeyList](s"/$kind/$lang/$entity/keysList",q)
  def schema(kind:String, lang:String, entity:String): Future[JSONSchema] = client.get[JSONSchema](s"/$kind/$lang/$entity/schema")
  def form(kind:String, lang:String, entity:String): Future[JSONMetadata] = client.get[JSONMetadata](s"/$kind/$lang/$entity/metadata")
  def subforms(entity:String, lang:String): Future[Seq[JSONMetadata]] = client.get[Seq[JSONMetadata]](s"/form/$lang/$entity/subform")
  def count(kind:String, lang:String, entity:String): Future[Int] = client.get[Int](s"/$kind/$lang/$entity/count")
  def insert(kind:String, lang:String, entity:String, data:Json): Future[Json] = client.post[Json,Json](s"/$kind/$lang/$entity",data)
  def get(kind:String, lang:String, entity:String, keys:JSONKeys):Future[Json] = {
    client.get[Json](s"/$kind/$lang/$entity/id/${keys.asString}")
  }
  def delete(kind:String, lang:String, entity:String, keys:JSONKeys):Future[JSONCount] = {
    client.delete[JSONCount](s"/$kind/$lang/$entity/id/${keys.asString}")
  }
  def update(kind:String, lang:String, entity:String, keys:JSONKeys, data:Json):Future[Json] = client.put[Json,Json](s"/$kind/$lang/$entity/id/${keys.asString}",data)
  def loginCheck() = client.get[String]("/checkLogin")
  def labels(lang:String):Future[Map[String,String]] = client.get[Map[String,String]](s"/labels/$lang")
  def conf():Future[Map[String,String]] = client.get[Map[String,String]](s"/conf")
}
