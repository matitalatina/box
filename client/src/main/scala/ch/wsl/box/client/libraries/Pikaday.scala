package ch.wsl.box.client.libraries


import ch.wsl.box.client.utils.Log
import org.scalajs.dom
import org.scalajs.dom.{Node, NodeListOf}
import org.scalajs.dom.raw.HTMLInputElement

import scala.scalajs.js
import scala.scalajs.js.{Any, Dictionary}

/**
  * Created by andre on 2/6/2017.
  */
trait PikadayOptions{
  val field:Node
  val format:String
  def onSelect:(js.Any) => Unit

  def toDict():js.Dictionary[js.Any] = {
    val dict:js.Dictionary[js.Any] = js.Dictionary()
    dict.update("field",field)
    dict.update("format",format)
    dict.update("onSelect",onSelect)
    dict
  }
}

/**
  * https://github.com/dbushell/Pikaday
  *
  * @param opts
  */
@js.native
class Pikaday(opts:js.Dictionary[js.Any]) extends js.Object {

}

object Pikaday{

  def mount(formatString:String = "YYYY-MM-DD")(n:Node):Pikaday = {
    val opts = new PikadayOptions {
      override def onSelect: (js.Any) => Unit = (date) => {
        val event = dom.document.createEvent("HTMLEvents")
        event.initEvent("input", true, false)
        field.dispatchEvent(event)
      }

      override val field: Node = n
      override val format: String = formatString
    }.toDict()
    Log.console(opts)
    new Pikaday(opts)
  }

  def apply(className:String,formatString:String = "YYYY-MM-DD") = {
    val datepickers = org.scalajs.dom.document.getElementsByClassName(className).asInstanceOf[NodeListOf[Node]]
    for{
      i <- 0 to datepickers.length
    } {
      mount(formatString)(datepickers.item(i))
    }
  }

}