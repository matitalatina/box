package ch.wsl.box.client.services

import ch.wsl.box.model.shared.{JSONField, JSONKey, JSONKeys, JSONSchema}
import io.circe.Json

import scala.concurrent.Future

/**
  * Created by andre on 4/24/2017.
  */
object REST {


  import io.circe.generic.auto._

  def username = "postgres"
  def password = ""

  private def client = HttpClient("http://localhost:8080/api/v1",username,password)


  def models():Future[Seq[String]] = client.get[Seq[String]]("/models")
  def list(model:String): Future[Seq[Json]] = client.get[Seq[Json]](s"/$model")
  def keys(model:String): Future[Seq[String]] = client.get[Seq[String]](s"/$model/keys")
  def schema(model:String): Future[JSONSchema] = client.get[JSONSchema](s"/$model/schema")
  def form(model:String): Future[Seq[JSONField]] = client.get[Seq[JSONField]](s"/$model/form")
  def count(model:String): Future[Int] = client.get[Int](s"/$model/count")
  def insert(model:String, data:Json): Future[String] = client.post[Json,String](s"/$model",data)
  def get(model:String,keys:JSONKeys):Future[Json] = {
    client.get[Json](s"/$model/id/${keys.asString}")
  }
  def update(model:String,keys:JSONKeys,data:Json):Future[String] = client.put[Json,String](s"/$model/id/${keys.asString}",data)
}
