package ch.wsl.box.client.services

import io.udash.rest._

import scala.concurrent.Future
import ch.wsl.box.client.utils.Base64._
import org.scalajs.dom

/**
  * Created by andre on 4/3/2017.
  */
@REST
trait BoxREST {
  @GET def models(@Header @RESTName("Authorization") authToken:String ):Future[Seq[String]]
  @GET def forms():Future[Seq[String]]
}

object RestClient {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  val server = DefaultServerREST[BoxREST](
    dom.window.location.hostname,
    dom.window.location.port.toInt,
    "/api/v1")

  def basicAuthToken(username: String, password: String):String = "Basic " + (username + ":" + password).getBytes.toBase64
}