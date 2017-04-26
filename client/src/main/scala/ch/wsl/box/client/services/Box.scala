package ch.wsl.box.client.services

import ch.wsl.box.model.shared.{JSONField, JSONSchema, JSONSchemaL2, JSONSchemaL3}
import io.circe.Json
import org.scalajs.dom

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
  * Created by andre on 4/24/2017.
  */
object Box {


  import io.circe.generic.auto._

  def username = "postgres"
  def password = ""

  private def client = HttpClient("http://localhost:8080/api/v1",username,password)


  def models():Future[Seq[String]] = client.get[Seq[String]]("/models")
  def list(model:String) = client.get[Seq[Json]](s"/$model")
  def schema(model:String) = client.get[JSONSchema](s"/$model/schema")
  def form(model:String) = client.get[Seq[JSONField]](s"/$model/form")
  def count(model:String) = client.get[Int](s"/$model/count")
}
