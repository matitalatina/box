package ch.wsl.box.client.views.components.widget

import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash._
import io.udash.properties.single.Property
import ch.wsl.box.shared.utils.JsonUtils._
import io.circe._
import io.circe.syntax._
import ch.wsl.box.client.Context._
import ch.wsl.box.client.styles.GlobalStyles
import scalacss.internal.StyleA
import scalatags.JsDom

import scala.util.Try


object DateTimeWidget {
  final val dateTimePickerFormat = "YYYY-MM-DD HH:mm"
  final val datePickerFormat = "YYYY-MM-DD"
  final val timePickerFormat = "HH:mm"

  import scalatags.JsDom.all._

  case class Date(key: Property[String], label: String, prop: Property[Json]) extends Widget {
    override def edit() = datetimepicker(key,label,prop,datePickerFormat,GlobalStyles.dateTimePicker)
    override protected def show(): JsDom.all.Modifier = showdate(label,prop)
  }

  case class DateTime(key: Property[String], label: String, prop: Property[Json]) extends Widget {
    override def edit() = datetimepicker(key,label,prop,dateTimePickerFormat,GlobalStyles.dateTimePicker)
    override protected def show(): JsDom.all.Modifier = showdate(label,prop)
  }

  case class Time(key: Property[String], label: String, prop: Property[Json]) extends Widget {
    override def edit() = datetimepicker(key,label,prop,timePickerFormat,GlobalStyles.dateTimePicker)
    override protected def show(): JsDom.all.Modifier = showdate(label,prop)
  }

  case class DateFullWidth(key: Property[String], label: String, prop: Property[Json]) extends Widget {
    override def edit() = datetimepicker(key,label,prop,datePickerFormat,GlobalStyles.dateTimePickerFullWidth)
    override protected def show(): JsDom.all.Modifier = showdate(label,prop)
  }

  case class DateTimeFullWidth(key: Property[String], label: String, prop: Property[Json]) extends Widget {
    override def edit() = datetimepicker(key,label,prop,dateTimePickerFormat,GlobalStyles.dateTimePickerFullWidth)
    override protected def show(): JsDom.all.Modifier = showdate(label,prop)
  }

  case class TimeFullWidth(key: Property[String], label: String, prop: Property[Json]) extends Widget {
    override def edit() = datetimepicker(key,label,prop,timePickerFormat,GlobalStyles.dateTimePickerFullWidth)
    override protected def show(): JsDom.all.Modifier = showdate(label,prop)
  }


  private def toDate(format:String)(jsonDate:Json):Option[java.util.Date] = Try{
    if(jsonDate == Json.Null) return None
    val str = jsonDate.string.trim
    if(str == "") return None
    format match {
      case `timePickerFormat` => {
        val year = 1970
        val month = 1
        val day = 1
        val timeTokens = str.split(":")
        val hours =  timeTokens(0).toInt
        val minutes = timeTokens(1).toInt
        new java.util.Date(year-1900,month-1,day,hours,minutes)
      }
      case `dateTimePickerFormat` => {
        val tokens = str.split(" ")
        val dateTokens = tokens(0).split("-")
        val timeTokens = tokens(1).split(":")
        val year = dateTokens(0).toInt
        val month = dateTokens(1).toInt
        val day = dateTokens(2).toInt
        val hours = timeTokens(0).toInt
        val minutes = timeTokens(1).toInt
        val result = new java.util.Date(year-1900,month-1,day,hours,minutes)
        result
      }
      case `datePickerFormat` => {
        val dateTokens = str.split("-")
        val year = dateTokens(0).toInt
        val month = dateTokens(1).toInt
        val day = dateTokens(2).toInt
        val result = new java.util.Date(year-1900,month-1,day)
        result
      }
    }

  }.toOption

  private def fromDate(format:String)(dt:Option[java.util.Date]):Json = {
    Try{
      if (!dt.isDefined)
        Json.Null
      else {
        val date = new scala.scalajs.js.Date(dt.get.getTime)
        if (date.getFullYear() == 1970 && date.getMonth() == 0 && date.getDate() == 1) return Json.Null
        val result = format match {
          case `dateTimePickerFormat` => date.getFullYear() + "-" + "%02d".format(date.getMonth() + 1) + "-" + "%02d".format(date.getDate()) + " " + "%02d".format(date.getHours()) + ":" + "%02d".format(date.getSeconds())
          case `datePickerFormat` => date.getFullYear() + "-" + "%02d".format(date.getMonth() + 1) + "-" + "%02d".format(date.getDate())
          case `timePickerFormat` => "%02d".format(date.getHours()) + ":" + "%02d".format(date.getMinutes())
        }
        result.asJson
      }
    }.getOrElse(Json.Null)
  }

  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._

  private def showdate(modelLabel:String, model:Property[Json]):Modifier = WidgetUtils.showNotNull(model){ p =>
    div(if (modelLabel.length > 0) label(modelLabel) else {},
      div(BootstrapStyles.pullRight,p.string),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  }

  private def datetimepicker(key:Property[String],modelLabel:String, model:Property[Json], format:String, style:StyleA):Modifier = {

    val date = model.transform(toDate(format),fromDate(format))

    val pickerOptions = ModelProperty(UdashDatePicker.DatePickerOptions(
      format = format,
      locale = Some("en_GB"),
      showClear = true,
      useStrict = false
    ))

    produce(key) { k =>
      val picker: UdashDatePicker = UdashDatePicker()(date, pickerOptions)
      div(
        if (modelLabel.length > 0) label(modelLabel) else {},
        div(style)(
          picker.render
        ),
        div(BootstrapStyles.Visibility.clearfix)
      ).render
    }
  }
}
