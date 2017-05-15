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
  def list(kind:String,model:String,limit:Int): Future[Seq[Json]] = client.post[JSONQuery,JSONResult[Json]](s"/$kind/$model/list",JSONQuery.limit(limit)).map(_.data)
  def csv(kind:String,model:String,q:JSONQuery): Future[Seq[Seq[String]]] = client.postString[JSONQuery](s"/$kind/$model/csv",q).map{ result =>
    result.split("\r\n").toSeq.map{x => x.substring(1,x.length-1).split("\",\"").toSeq}
  }
  def keys(kind:String,model:String): Future[Seq[String]] = client.get[Seq[String]](s"/$kind/$model/keys")
  def schema(kind:String,model:String): Future[JSONSchema] = client.get[JSONSchema](s"/$kind/$model/schema")
  def form(kind:String,model:String): Future[Seq[JSONField]] = client.get[Seq[JSONField]](s"/$kind/$model/form")
  def count(kind:String,model:String): Future[Int] = client.get[Int](s"/$kind/$model/count")
  def insert(kind:String,model:String, data:Json): Future[Json] = client.post[Json,Json](s"/$kind/$model",data)
  def get(kind:String,model:String,keys:JSONKeys):Future[Json] = {
    client.get[Json](s"/$kind/$model/id/${keys.asString}")
  }
  def update(kind:String,model:String,keys:JSONKeys,data:Json):Future[String] = client.put[Json,String](s"/$kind/$model/id/${keys.asString}",data)
}
