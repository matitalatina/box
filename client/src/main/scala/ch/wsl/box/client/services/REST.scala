package ch.wsl.box.client.services

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

  def sendFile(file:File,keys:JSONKeys,model:String) = client.sendFile[Int](s"/file/$model/${keys.asString}",file)
  def models(kind:String):Future[Seq[String]] = client.get[Seq[String]](s"/${kind}s")
  def list(kind:String,lang:String,model:String,limit:Int): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$kind/$lang/$model/list",JSONQuery.limit(limit)).map(_.data)
  def list(kind:String,lang:String,model:String,query:JSONQuery): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$kind/$lang/$model/list",query).map(_.data)
  def csv(kind:String,lang:String,model:String,q:JSONQuery): Future[Seq[Seq[String]]] = client.post[JSONQuery,String](s"/$kind/$lang/$model/csv",q).map{ result =>
    result.split("\r\n").toSeq.map{x => x.substring(1,x.length-1).split("\",\"").toSeq}
  }
  def keys(kind:String,lang:String,model:String): Future[Seq[String]] = client.get[Seq[String]](s"/$kind/$lang/$model/keys")
  def keysList(kind:String,lang:String,model:String,q:JSONQuery): Future[KeyList] = client.post[JSONQuery,KeyList](s"/$kind/$lang/$model/keysList",q)
  def schema(kind:String,lang:String,model:String): Future[JSONSchema] = client.get[JSONSchema](s"/$kind/$lang/$model/schema")
  def form(kind:String,lang:String,model:String): Future[JSONMetadata] = client.get[JSONMetadata](s"/$kind/$lang/$model/metadata")
  def subforms(model:String,lang:String): Future[Seq[JSONMetadata]] = client.get[Seq[JSONMetadata]](s"/form/$lang/$model/subform")
  def count(kind:String,lang:String,model:String): Future[Int] = client.get[Int](s"/$kind/$lang/$model/count")
  def insert(kind:String,lang:String,model:String, data:Json): Future[Json] = client.post[Json,Json](s"/$kind/$lang/$model",data)
  def get(kind:String,lang:String,model:String,keys:JSONKeys):Future[Json] = {
    client.get[Json](s"/$kind/$lang/$model/id/${keys.asString}")
  }
  def delete(kind:String,lang:String,model:String,keys:JSONKeys):Future[JSONCount] = {
    client.delete[JSONCount](s"/$kind/$lang/$model/id/${keys.asString}")
  }
  def update(kind:String,lang:String,model:String,keys:JSONKeys,data:Json):Future[Json] = client.put[Json,Json](s"/$kind/$lang/$model/id/${keys.asString}",data)
  def loginCheck() = client.get[String]("/checkLogin")
  def labels(lang:String):Future[Map[String,String]] = client.get[Map[String,String]](s"/labels/$lang")
  def conf():Future[Map[String,String]] = client.get[Map[String,String]](s"/conf")
}
