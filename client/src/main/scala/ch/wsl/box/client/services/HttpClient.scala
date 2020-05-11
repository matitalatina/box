package ch.wsl.box.client.services

import java.io.ByteArrayInputStream

import ch.wsl.box.client.IndexState
import ch.wsl.box.client.utils.Session
import ch.wsl.box.model.shared.errors.{ExceptionReport, GenericExceptionReport, JsonDecoderExceptionReport, SQLExceptionReport}
import org.scalajs.dom
import org.scalajs.dom.{File, FormData, XMLHttpRequest}
import scribe.Logging

import scala.concurrent.{Future, Promise}

/**
  * Created by andre on 4/26/2017.
  */
object HttpClient{
  type Response[T] = Either[ExceptionReport,T]
}

case class HttpClient(_endpoint:String) extends Logging {

  lazy val endpoint = {
    dom.window.location.port == "12345" match {
      case false => _endpoint
      case true => "http://localhost:8080/" + _endpoint
    }
  }

  import io.circe.parser.decode
  import io.circe.syntax._
  import io.circe.parser._
  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.JSONUtils._
  import HttpClient._

  import ch.wsl.box.client.Context._
  import scala.concurrent.blocking


  private def httpCall[T](method:String, url:String, json:Boolean=true, file:Boolean=false)(send:XMLHttpRequest => Unit)(implicit decoder:io.circe.Decoder[T]):Future[Response[T]] = {


    val promise = Promise[Response[T]]()
    blocking {
      val xhr = new dom.XMLHttpRequest()
      xhr.open(method, endpoint + url, false)
      xhr.withCredentials = true
      xhr.setRequestHeader("Cache-Control","no-store")
      if (json) {
        xhr.setRequestHeader("Content-Type", "application/json")
      }
      if (file) {
        xhr.setRequestHeader("Content-Type", "application/octet-stream")
      }
      xhr.onload = { (e: dom.Event) =>
        if (xhr.status == 200) {
          if (xhr.getResponseHeader("Content-Type").contains("text")) {
            promise.success(Right(xhr.responseText.asInstanceOf[T]))

          } else if (xhr.getResponseHeader("Content-Type").contains("application/octet-stream")) {
            promise.success(Right(xhr.response.asInstanceOf[T]))

          } else {
            decode[T](xhr.responseText) match {
              case Left(fail) => {
                logger.warn(s"Failed to decode JSON on $url with error: $fail")
                promise.failure(fail)
              }
              case Right(result) => promise.success(Right(result))
            }
          }
        } else if (xhr.status == 401 || xhr.status == 403) {
          logger.info("Not authorized")
          Session.logoutAndSaveState()
          promise.failure(new Exception("HTTP status" + xhr.status))
        } else {
          promise.success(Left(manageError(xhr)))
        }
      }


      xhr.onerror = { (e: dom.Event) =>
        if (xhr.status == 401 || xhr.status == 403) {
          logger.info("Not authorized")
          Session.logoutAndSaveState()
          promise.failure(new Exception("HTTP status" + xhr.status))
        } else {
          promise.success(Left(manageError(xhr)))
        }
      }

      send(xhr)
    }

    promise.future

  }

  def manageError[T](xhr:dom.XMLHttpRequest):ExceptionReport = {
    if(xhr.responseText == null) {
      GenericExceptionReport(s"HTTP response code ${xhr.status}, no body returned")
    } else {
      {
        for{
          json <- {
            val r = parse(xhr.responseText).right.toOption
            logger.debug(r.toString)
            r
          }
          er <- {
            val r = json.getOpt("source").flatMap{
              case "json" => {
                val r = json.as[JsonDecoderExceptionReport]
                logger.debug(r.toString)
                r.right.toOption
              }
              case "sql" => json.as[SQLExceptionReport].right.toOption
              case x =>  None
            }
            logger.debug(r.toString)
            r
          }
        } yield er
      }.getOrElse(GenericExceptionReport(xhr.responseText))
    }
  }

  private def httpCallWithNoticeInterceptor[T](method:String, url:String, json:Boolean=true, file:Boolean=false)(send:XMLHttpRequest => Unit)(implicit decoder:io.circe.Decoder[T]):Future[T] = httpCall(method,url,json,file)(send).map{
    case Right(result) => result
    case Left(error) => {
      Notification.add(error.humanReadable(Map()))
      throw new Exception(error.toString)
    }
  }

  private def request[T](method:String,url:String)(implicit decoder:io.circe.Decoder[T]):Future[T] = httpCallWithNoticeInterceptor[T](method,url)( xhr => xhr.send())

  private def send[D,R](method:String,url:String,obj:D,json:Boolean = true)(implicit decoder:io.circe.Decoder[R],encoder: io.circe.Encoder[D]):Future[R] = {
    httpCallWithNoticeInterceptor[R](method,url,json){ xhr =>
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

    httpCallWithNoticeInterceptor[T]("POST",url,false){ xhr =>
      xhr.send(formData)
    }

  }

//  def getFile[T](url:String):Future[T] = {
//    httpCall("GET", url, false, true)
////    val formData = new FormData();
////    formData.append("file",file)
////
////    httpCall[T]("GET",url,false){ xhr =>
////      xhr.send(formData)
////    }
//
//  }




}

