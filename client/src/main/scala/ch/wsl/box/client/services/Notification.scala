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

  private var socket:WebSocket = null

  def setUpWebsocket(): Unit = {

    if(socket != null) {
      socket.close()
    }

    socket = new WebSocket(Routes.wsV1("box-client"))

    socket.onmessage = (msg => {

      for{
        js <- parse(msg.data.toString).toOption
        notification <- js.as[NotificationMessage].toOption
      } yield {
        add(notification.body)
      }


    })
  }

  def closeWebsocket(): Unit = {
    if(socket != null) {
      socket.close()
    }
  }

  def add(notice:String) = {
    _list.append(notice)
    setTimeout(ClientConf.notificationTimeOut seconds){
      _list.remove(notice)
    }
  }
}
