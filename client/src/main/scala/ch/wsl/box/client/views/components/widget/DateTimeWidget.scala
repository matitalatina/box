package ch.wsl.box.client.views.components.widget

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash._
import ch.wsl.box.shared.utils.JSONUtils._
import io.circe._
import io.circe.syntax._
import ch.wsl.box.client.Context._
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.{BrowserConsole, ClientConf}
import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes, WidgetsNames}
import ch.wsl.box.shared.utils.DateTimeFormatters
import io.udash.bootstrap.datepicker.UdashDatePicker.Placement
import io.udash.properties.single.Property
import org.scalajs.dom.{Event, KeyboardEvent}
import scalacss.internal.StyleA
import scalatags.JsDom
import scribe.Logging
import typings.flatpickr.optionsMod.{DateOption, Hook}
import typings.std.HTMLInputElement

import scala.scalajs.js
import scala.util.Try
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.|

trait DateTimeWidget extends Widget  with Logging{

  import scalatags.JsDom.all._
  import io.udash.css.CssView._
  import scalacss.ScalatagsCss._

  final val dateTimePickerFormat = "YYYY-MM-DD HH:mm"
  final val datePickerFormat = "YYYY-MM-DD"
  final val yearPickerFormat = "YYYY"
  final val yearMonthPickerFormat = "YYYY-MM"
  final val timePickerFormat = "HH:mm"

  private def strToTime(s:String,r:Boolean) = {
    DateTimeFormatters.timestamp.parse(s).toArray.flatMap{ parsed =>
      def toDouble(d:LocalDateTime):DateOption = d.toInstant(ZoneOffset.UTC).toEpochMilli.toDouble
      (r,s.length) match {
        case (false, _) => Array(toDouble(parsed))
        case (true, x) if x > 7 => Array(toDouble(parsed))
        case (true, x) if x > 4 => { //month interval
          logger.info(s"Expand month")
          Array(toDouble(parsed),toDouble(parsed.plusMonths(1)))
        }
        case (true, x) => { //year interval
          logger.info(s"Expand year")
          Array(toDouble(parsed),toDouble(parsed.plusYears(1)))
        }
      }
    }
  }

  protected def toDate(jsonDate:Json,range:Boolean):Array[DateOption] = {
    logger.info(s"toDate $jsonDate")
    if(jsonDate == Json.Null) return Array()
    val str = jsonDate.string.trim
    if(str == "") return Array()



    if(range) {
      val tokens = str.split("to").map(_.trim)
      if(tokens.length > 1)
        tokens.flatMap(t => strToTime(t,false))
      else
        strToTime(str,true)
    } else {
      strToTime(str,false)
    }


  }

  protected def fromDate(format:String)(dt:Option[java.util.Date]):Json = {
    logger.info(s"fromDate $format date $dt")
    Try{
      if (dt.isEmpty)
        Json.Null
      else {
        val instant = Instant.ofEpochMilli(dt.get.getTime).atZone(ZoneOffset.UTC)
        val result = format match {
          case `dateTimePickerFormat` => DateTimeFormatters.timestamp.format(instant.toLocalDateTime)
          case `datePickerFormat` => DateTimeFormatters.date.format(instant.toLocalDate)
          case `timePickerFormat` => DateTimeFormatters.time.format(instant.toLocalTime)
        }
        println(result)
        result.asJson
      }
    }.recover{case e =>
      e.printStackTrace()
      Json.Null
    }.get
  }


  protected def showMe(modelLabel:String, model:Property[Json]):Modifier = autoRelease(WidgetUtils.showNotNull(model){ p =>
    div(if (modelLabel.length > 0) label(modelLabel) else {},
      div(BootstrapStyles.Float.right(), bind(model.transform(_.string))),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  })

  protected def editMe(id:Property[String], field:JSONField, model:Property[Json], format:String, style:StyleA, range:Boolean):Modifier = {

    val tooltip = WidgetUtils.addTooltip(field.tooltip) _

    var flatpicker:typings.flatpickr.instanceMod.Instance = null

    def handleDate(d:Json,force:Boolean = false): Unit = {
      if(range) {
        val dates = toDate(d,range).toJSArray
        flatpicker.setDate(dates,force)
      } else {
        toDate(d,range).headOption.foreach( date => flatpicker.setDate(date,force))
      }
    }

    val picker = input(
      style,if(field.nullable) {} else ClientConf.style.notNullable,
      onkeydown := { (e: KeyboardEvent) =>
        e.stopPropagation()
        e.keyCode match {
          case 13 => {
            handleDate(e.target.asInstanceOf[HTMLInputElement].value.asJson, true)
            flatpicker.close()
          }
          case _ => {}
        }
      },
      onblur := { (e: Event) =>
        e.stopPropagation()
        handleDate(e.target.asInstanceOf[HTMLInputElement].value.asJson, true)
      },
    ).render
    var changeListener:Registration = null



    def setListener(immediate: Boolean, flatpicker:typings.flatpickr.instanceMod.Instance) = {
      changeListener = model.listen({ d =>
        logger.info(s"Changed model to $d")
        handleDate(d)
      },immediate)
    }


    val result = div(BootstrapCol.md(12),ClientConf.style.noPadding,ClientConf.style.smallBottomMargin,
      if (field.title.length > 0) WidgetUtils.toLabel(field, false) else {},
      tooltip(picker),
      div(BootstrapStyles.Visibility.clearfix)
    ).render

    val onChange:Hook = (
                          selectedDates:js.Array[typings.flatpickr.globalsMod.global.Date],
                          dateStr:String,
                          instance: typings.flatpickr.instanceMod.Instance,
                          data:js.UndefOr[js.Any]) => {
      changeListener.cancel()
      logger.info(s"flatpickr on change $dateStr")
      model.set(dateStr.asJson)
      setListener(false, instance)
    }

    val options = typings.flatpickr.optionsMod.Options()
      .setAllowInput(true)
      .setOnChange(onChange)

    if(range) {
      options.setMode(typings.flatpickr.flatpickrStrings.range)
    }

    format match {
      case `dateTimePickerFormat` => options.setEnableTime(true).setTime_24hr(true)
      case `datePickerFormat` => {}
      case `timePickerFormat` => options.setEnableTime(true).setTime_24hr(true).setNoCalendar(true).setDateFormat("H:i")
    }

    flatpicker = typings.flatpickr.mod.default(picker,options)

    setListener(true,flatpicker)


    result

  }
}

object DateTimeWidget {




  case class Date(id: Property[String], field: JSONField, prop: Property[Json], range:Boolean = false) extends DateTimeWidget {
    override def edit() = editMe(id,field,prop,datePickerFormat,ClientConf.style.dateTimePicker,range)
    override protected def show(): JsDom.all.Modifier = showMe(field.title,prop)
  }

  object Date extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = Date(id,field,prop)
  }

  case class DateTime(id: Property[String], field: JSONField, prop: Property[Json], range:Boolean = false) extends DateTimeWidget {
    override def edit() = editMe(id,field,prop,dateTimePickerFormat,ClientConf.style.dateTimePicker,range)
    override protected def show(): JsDom.all.Modifier = showMe(field.title,prop)
  }

  object DateTime extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = DateTime(id,field,prop)
  }

  case class Time(id: Property[String], field: JSONField, prop: Property[Json], range:Boolean = false) extends DateTimeWidget {
    override def edit() = editMe(id,field,prop,timePickerFormat,ClientConf.style.dateTimePicker,range)
    override protected def show(): JsDom.all.Modifier = showMe(field.title,prop)
  }

  object Time extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = Time(id,field,prop)
  }

  case class DateFullWidth(id: Property[String], field: JSONField, prop: Property[Json], range:Boolean = false) extends DateTimeWidget {
    override def edit() = editMe(id,field,prop,datePickerFormat,ClientConf.style.dateTimePickerFullWidth,range)
    override protected def show(): JsDom.all.Modifier = showMe(field.title,prop)
  }

  object DateFullWidth extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = DateFullWidth(id,field,prop)
  }

  case class DateTimeFullWidth(id: Property[String], field: JSONField, prop: Property[Json], range:Boolean = false) extends DateTimeWidget {
    override def edit() = editMe(id,field,prop,dateTimePickerFormat,ClientConf.style.dateTimePickerFullWidth,range)
    override protected def show(): JsDom.all.Modifier = showMe(field.title,prop)
  }

  object DateTimeFullWidth extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = DateTimeFullWidth(id,field,prop)
  }

  case class TimeFullWidth(id: Property[String], field: JSONField, prop: Property[Json], range:Boolean = false) extends DateTimeWidget {
    override def edit() = editMe(id,field,prop,timePickerFormat,ClientConf.style.dateTimePickerFullWidth,range)
    override protected def show(): JsDom.all.Modifier = showMe(field.title,prop)
  }

  object TimeFullWidth extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = TimeFullWidth(id,field,prop)
  }



}
