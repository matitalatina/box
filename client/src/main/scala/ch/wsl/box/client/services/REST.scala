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
  def list(model:String,limit:Int): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$model/list",JSONQuery.limit(limit)).map(_.data)
  def csv(model:String,q:JSONQuery): Future[Seq[Seq[String]]] = client.postString[JSONQuery](s"/$model/csv",q).map{ result =>
    result.split("\r\n").toSeq.map{x => x.substring(1,x.length-1).split("\",\"").toSeq}
  }
  def keys(model:String): Future[Seq[String]] = client.get[Seq[String]](s"/$model/keys")
  def schema(model:String): Future[JSONSchema] = client.get[JSONSchema](s"/$model/schema")
  def form(model:String): Future[Seq[JSONField]] = client.get[Seq[JSONField]](s"/$model/form")
  def count(model:String): Future[Int] = client.get[Int](s"/$model/count")
  def insert(model:String, data:Json): Future[Json] = client.post[Json,Json](s"/$model",data)
  def get(model:String,keys:JSONKeys):Future[Json] = {
    client.get[Json](s"/$model/id/${keys.asString}")
  }
  def update(model:String,keys:JSONKeys,data:Json):Future[String] = client.put[Json,String](s"/$model/id/${keys.asString}",data)
}
