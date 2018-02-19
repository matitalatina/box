package ch.wsl.box.client.services

import io.udash._
import scalajs.js.timers._
import scala.concurrent.duration._
import ch.wsl.box.client.Context._

object Notification {
  private val _list:SeqProperty[String] = SeqProperty(Seq[String]())
  def list:ReadableSeqProperty[String] = _list

  def add(notice:String) = {
    _list.append(notice)
    setTimeout(5 seconds){
      _list.remove(notice)
    }
  }
}
