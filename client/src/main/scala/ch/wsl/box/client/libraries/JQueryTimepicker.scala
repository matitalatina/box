package ch.wsl.box.client.libraries

import org.scalajs.jquery.{JQuery, jQuery}
import io.circe._
import io.circe.generic.auto._
import io.circe.scalajs._
import io.circe.syntax._
import japgolly.scalajs.react.Callback
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Any

/**
  * Created by andre on 3/7/2017.
  */
object JQueryTimepicker {
  implicit class toTimepicker(jQueryDyn: JQuery) {
    val el = jQueryDyn.asInstanceOf[TimepickerJQueryPlugin]
    def timepicker(opts:TimepickerOptions) = {
      val jsOpts = opts.asJsAny.asInstanceOf[js.Dictionary[js.Any]]
      jsOpts.update("change",{ time:js.Any =>
        val event = dom.document.createEvent("HTMLEvents")
        event.initEvent("input", true, false)
        el.each(_.dispatchEvent(event))
      });



      el.timepicker(jsOpts)
    }
  }

  def apply(className:String,opts:TimepickerOptions = TimepickerOptions()) = {
    jQuery("."+className).timepicker(opts)
  }
}

@js.native
trait TimepickerJQueryPlugin extends JQuery{
  def timepicker(opts:js.Dictionary[js.Any]):js.Dynamic
}

//@js.native
//trait TimepickerOptionsJs extends js.Any{
//  val timeFormat:String
//  val interval:Int
//  val minTime:String
//  val maxTime:String
//  val defaultTime:String
//  val startTime:String
//  val dynamic: Boolean
//  val dropdown: Boolean
//  val scrollbar: Boolean
//  def change(time:js.Any):js.Any
//}




case class TimepickerOptions(
                              timeFormat:String = "HH:mm:ss",
                              interval:Int = 30,
                              minTime:String = "0",
                              maxTime:String = "23:59",
                              defaultTime:String = "12",
                              startTime:String = "00:00",
                              dynamic: Boolean = true,
                              dropdown: Boolean = true,
                              scrollbar: Boolean = true
                            )