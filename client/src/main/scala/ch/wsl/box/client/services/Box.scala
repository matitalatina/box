package ch.wsl.box.client.services

import ch.wsl.box.model.shared.{JSONSchema, JSONSchemaL2, JSONSchemaL3}
import org.scalajs.dom
import upickle.Js

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
  * Created by andre on 4/24/2017.
  */
object Box {


  import upickle.default._
  import ch.wsl.box.client.formatters.Formatters._


  def username = "postgres"
  def password = ""

  private def client = HttpClient("http://localhost:8080/api/v1",username,password)


  def models():Future[Seq[String]] = client.get[Seq[String]]("/models")
  def list(model:String) = client.get[Seq[Js.Value]](s"/$model")
  def schema(model:String) = client.get[JSONSchema](s"/$model/schema")
}
