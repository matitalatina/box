package ch.wsl.box.client.services

import ch.wsl.box.client.IndexState
import ch.wsl.box.client.utils.Session
import org.scalajs.dom
import org.scalajs.dom.{File, FormData, XMLHttpRequest}

import scala.concurrent.{Future, Promise}

/**
  * Created by andre on 4/26/2017.
  */
case class HttpClient(endpoint:String) {


  import io.circe.parser.decode
  import io.circe.syntax._

  import ch.wsl.box.client.Context._

  private def httpCall[T](method:String,url:String,json:Boolean = true)(send:XMLHttpRequest => Unit)(implicit decoder:io.circe.Decoder[T]):Future[T] = {
    val xhr = new dom.XMLHttpRequest()

    val promise = Promise[T]()

    xhr.open(method,endpoint+url,false)
    xhr.setRequestHeader("Authorization",Session.authToken())
    if(json) {
      xhr.setRequestHeader("Content-Type", "application/json")
    }
    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        if(xhr.getResponseHeader("Content-Type").contains("text/plain")) {
          promise.success(xhr.responseText.asInstanceOf[T])
        } else {
          decode[T](xhr.responseText) match {
            case Left(fail) => {
              println(s"Failed to decode JSON on $url with error: $fail")
              promise.failure(fail)
            }
            case Right(result) => promise.success(result)
          }
        }
      } else if (xhr.status == 401) {
        println("Non autorizzato")
        Session.logout()
        promise.failure(new Exception("HTTP status" + xhr.status))
      } else {
        promise.failure(new Exception("HTTP status" + xhr.status))
      }
    }

    xhr.onerror = { (e: dom.Event) =>
      promise.failure(new Exception("Error HTTP status" + xhr.status))
    }

    send(xhr)

    promise.future

  }

  private def request[T](method:String,url:String)(implicit decoder:io.circe.Decoder[T]):Future[T] = httpCall[T](method,url)( xhr => xhr.send())

  private def send[D,R](method:String,url:String,obj:D)(implicit decoder:io.circe.Decoder[R],encoder: io.circe.Encoder[D]):Future[R] = {
    httpCall[R](method,url){ xhr =>
      xhr.send(obj.asJson.toString())
    }

  }



  def post[D,R](url:String,obj:D)(implicit decoder:io.circe.Decoder[R],encoder: io.circe.Encoder[D]) = send[D,R]("POST",url,obj)
  def put[D,R](url:String,obj:D)(implicit decoder:io.circe.Decoder[R],encoder: io.circe.Encoder[D]) = send[D,R]("PUT",url,obj)
  def get[T](url:String)(implicit decoder:io.circe.Decoder[T]):Future[T] = request("GET",url)
  def delete[T](url:String)(implicit decoder:io.circe.Decoder[T]):Future[T] = request("DELETE",url)

  def sendFile[T](url:String, file:File)(implicit decoder:io.circe.Decoder[T]):Future[T] = {

    val formData = new FormData();
    formData.append("file",file)

    httpCall[T]("POST",url,false){ xhr =>
      xhr.send(formData)
    }

  }






}

