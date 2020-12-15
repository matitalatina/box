package ch.wsl.box.client.services

import ch.wsl.box.client.routes.Routes
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.udash._
import org.scalajs.dom.WebSocket

import scala.concurrent.duration._
import scala.scalajs.js.timers.setTimeout

case class NotificationMessage(body:String)

object Notification {
  private val _list:SeqProperty[String] = SeqProperty(Seq[String]())
  def list:ReadableSeqProperty[String] = _list

  def setUpWebsocket(): Unit = {
    val exampleSocket = new WebSocket(Routes.wsV1("box-client"))
    exampleSocket.onmessage = (msg => {

      for{
        js <- parse(msg.data.toString).toOption
        notification <- js.as[NotificationMessage].toOption
      } yield {
        add(notification.body)
      }


    })
  }

  def add(notice:String) = {
    _list.append(notice)
    setTimeout(ClientConf.notificationTimeOut seconds){
      _list.remove(notice)
    }
  }
}
