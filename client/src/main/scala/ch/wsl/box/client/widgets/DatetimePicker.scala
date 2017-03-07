package ch.wsl.box.client.widgets

import ch.wsl.box.client.components.base.widget.{Widget, WidgetProps}
import ch.wsl.box.client.libraries.{JQueryTimepicker, Pikaday}
import ch.wsl.box.model.shared.WidgetsNames
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactElement, ReactEventI}

import scala.scalajs.js

/**
  * Created by andre on 3/7/2017.
  */
object DatetimePicker extends Widget {
  override def name: String = WidgetsNames.datetimePicker

  val classNameDate = "widget-datetime-date"
  val classNameTime = "widget-datetime-time"


  def splitDateTime(datetime: js.Any):(String,String) = {
    val splitted = datetime.toString.split(" ")
    if(splitted.size == 2) {
      (splitted(0),splitted(1))
    } else {
      ("","")
    }
  }

  def onChangeDate(wp:WidgetProps)(e: ReactEventI): Callback = Callback{
    val (date,time) = splitDateTime(wp.value)
    wp.onChange(e.target.value + " " + time)
  }

  def onChangeTime(wp:WidgetProps)(e: ReactEventI): Callback = Callback{
    val (date,time) = splitDateTime(wp.value)
    wp.onChange(date + " " + e.target.value)
  }

  override def render: (WidgetProps) => ReactElement = { P =>
    val (date,time) = splitDateTime(P.value)
    <.div(
      <.input(^.id := P.id + "-date" ,^.`type` := "text", ^.`class` := classNameDate, ^.defaultValue := date, ^.onChange ==> onChangeDate(P), ^.onInput ==> onChangeDate(P)),
      <.input(^.id := P.id + "-time" ,^.`type` := "text", ^.`class` := classNameTime, ^.defaultValue := time, ^.onChange ==> onChangeTime(P), ^.onInput ==> onChangeTime(P))
    )
  }

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  override def mount: Callback = Callback{
    JQueryTimepicker(classNameTime)
    Pikaday(classNameDate)
  } >> Callback.log("datetime widget mounted")
}
