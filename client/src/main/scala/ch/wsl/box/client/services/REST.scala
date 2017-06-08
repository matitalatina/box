package ch.wsl.box.client.services

import ch.wsl.box.model.shared._
import io.circe.Json

import scala.concurrent.Future

/**
  * Created by andre on 4/24/2017.
  */
object REST {


  import io.circe.generic.auto._
  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  def username = "postgres"
  def password = ""

  private def client = HttpClient("http://localhost:8080/api/v1",username,password)


  def models(kind:String):Future[Seq[String]] = client.get[Seq[String]](s"/${kind}s")
  def list(kind:String,lang:String,model:String,limit:Int): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$kind/$lang/$model/list",JSONQuery.limit(limit)).map(_.data)
  def list(kind:String,lang:String,model:String,query:JSONQuery): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$kind/$lang/$model/list",query).map(_.data)
  def csv(kind:String,lang:String,model:String,q:JSONQuery): Future[Seq[Seq[String]]] = client.postString[JSONQuery](s"/$kind/$lang/$model/csv",q).map{ result =>
    result.split("\r\n").toSeq.map{x => x.substring(1,x.length-1).split("\",\"").toSeq}
  }
  def keys(kind:String,lang:String,model:String): Future[Seq[String]] = client.get[Seq[String]](s"/$kind/$lang/$model/keys")
  def keysList(kind:String,lang:String,model:String,q:JSONQuery): Future[KeyList] = client.post[JSONQuery,KeyList](s"/$kind/$lang/$model/keysList",q)
  def schema(kind:String,lang:String,model:String): Future[JSONSchema] = client.get[JSONSchema](s"/$kind/$lang/$model/schema")
  def form(kind:String,lang:String,model:String): Future[JSONForm] = client.get[JSONForm](s"/$kind/$lang/$model/form")
  def subforms(model:String,lang:String): Future[Seq[JSONForm]] = client.get[Seq[JSONForm]](s"/form/$lang/$model/subform")
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
}
