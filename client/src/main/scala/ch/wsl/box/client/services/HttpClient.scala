package ch.wsl.box.client.services

import org.scalajs.dom

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
  * Created by andre on 4/26/2017.
  */
case class HttpClient(endpoint:String,user:String, password:String) {
  private val xhr = new dom.XMLHttpRequest()

  import ch.wsl.box.client.utils.Base64._
  private def basicAuthToken(username: String, password: String):String = "Basic " + (username + ":" + password).getBytes.toBase64


  import io.circe.syntax._
  import io.circe.parser.decode

  def get[T](url:String)(implicit decoder:io.circe.Decoder[T]):Future[T] = {
    val promise = Promise[T]()



    xhr.open("GET",endpoint+url,true)
    xhr.setRequestHeader("Authorization",basicAuthToken(user,password))
    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
          decode[T](xhr.responseText) match {
            case Left(fail) => promise.failure(fail)
            case Right(result) => promise.success(result)
          }
      } else {
        promise.failure(new Exception("HTTP status" + xhr.status))
      }
    }

    xhr.onerror = { (e: dom.Event) =>
        promise.failure(new Exception("Error HTTP status" + xhr.status))
    }

    xhr.send()

    promise.future

  }

  def post[D,R](url:String,obj:D)(implicit decoder:io.circe.Decoder[R],encoder: io.circe.Encoder[D]):Future[R] = {
    val promise = Promise[R]()

    xhr.open("POST",endpoint+url,true)
    xhr.setRequestHeader("Authorization",basicAuthToken(user,password))
    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        decode[R](xhr.responseText) match {
          case Left(fail) => promise.failure(fail)
          case Right(result) => promise.success(result)
        }
      } else {
        promise.failure(new Exception("HTTP status" + xhr.status))
      }
    }

    xhr.onerror = { (e: dom.Event) =>
      promise.failure(new Exception("Error HTTP status" + xhr.status))
    }

    xhr.send(obj.asJson.toString())

    promise.future

  }


}
