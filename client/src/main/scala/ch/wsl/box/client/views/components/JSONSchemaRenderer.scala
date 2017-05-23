package ch.wsl.box.client.views.components


import ch.wsl.box.model.shared.{JSONField, JSONFieldOptions, JSONSchema, WidgetsNames}
import io.circe.Json
import ch.wsl.box.shared.utils.JsonUtils._
import io.udash.properties.single.Property
import org.scalajs.dom.Element
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash.bootstrap.form.{UdashForm, UdashInputGroup}

import scala.scalajs.js.Date
import scalatags.JsDom.TypedTag




/**
  * Created by andre on 4/25/2017.
  */
object JSONSchemaRenderer {

  case class FormDefinition(fields:Seq[JSONField])

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe._
  import io.circe.syntax._


  final val dateTimePickerFormat = "YYYY-MM-DD HH:mm"
  final val datePickerFormat = "YYYY-MM-DD"
  final val timePickerFormat = "HH:mm"

  def datetimepicker(modelLabel:String, model:Property[String],format:String = dateTimePickerFormat):Modifier = {
    val pickerOptions = ModelProperty(UdashDatePicker.DatePickerOptions(
      format = format,
      locale = Some("en_GB"),
      showClear = true
    ))


    def toDate(str:String):java.util.Date = {
      if(str == "") return null;
      format match {
        case `timePickerFormat` => {
          val string = "1970-01-01 " + str
          new java.util.Date(Date.parse(string).toLong)
        }
        case _ => new java.util.Date(Date.parse(str).toLong)
      }

    }

    def fromDate(dt:java.util.Date):String = {
      val date = new Date(dt.getTime)
      if(date.getFullYear() == 1970 && date.getMonth() == 0 && date.getDate() == 1) return ""
      val result = format match {
        case `dateTimePickerFormat` => date.getFullYear() + "-" + "%02d".format(date.getMonth()+1) + "-" + "%02d".format(date.getDate()) + " " + "%02d".format(date.getHours()) + ":" + "%02d".format(date.getSeconds())
        case `datePickerFormat` => date.getFullYear() + "-" + "%02d".format(date.getMonth()+1) + "-" + "%02d".format(date.getDate())
        case `timePickerFormat` => "%02d".format(date.getHours()) + ":" +  "%02d".format(date.getMinutes())
      }
      result
    }


    val date = model.transform(toDate,fromDate)


    val picker: UdashDatePicker = UdashDatePicker()(date, pickerOptions)

    div(BootstrapStyles.Form.formGroup)(
      UdashDatePicker.loadBootstrapDatePickerStyles(),
      label(modelLabel),
      UdashInputGroup()(
        UdashInputGroup.input(picker.render),
        UdashInputGroup.addon("")
      ).render
    ).render
  }

  def optionsRenderer(modelLabel:String, options:JSONFieldOptions,model:Property[String]):Modifier = {
    div(BootstrapStyles.Form.formGroup)(
      label(modelLabel),
      Select(model,options.options.values.toSeq,BootstrapStyles.Form.formControl)
    )
  }


  def fieldRenderer(field:JSONField,model:Property[Json],keys:Seq[String],showLabel:Boolean = true):Modifier = {
    val label = showLabel match {
      case true => field.title.getOrElse(field.key)
      case false => ""
    }
    def jsToString(json:Json):String = json.string
    def strToJson(str:String):Json = str.asJson
    val stringModel = model.transform(jsToString,strToJson)
    (field.`type`,field.widget,field.options,keys.contains(field.key)) match {
      case (_,_,_,true) => UdashForm.textInput()(label)(stringModel,disabled := true)
      case (_,_,Some(options),_) => optionsRenderer(label,options,stringModel)
      case ("number",_,_,_) => UdashForm.numberInput()(label)(stringModel)
      case ("string",Some(WidgetsNames.timepicker),_,_) => datetimepicker(label,stringModel,timePickerFormat)
      case ("string",Some(WidgetsNames.datepicker),_,_) => datetimepicker(label,stringModel,datePickerFormat)
      case ("string",Some(WidgetsNames.datetimePicker),_,_) => datetimepicker(label,stringModel)
      case (_,_,_,_) => UdashForm.textInput()(label)(stringModel)
    }
  }

  def subform(label:String,formId:String) = ???

  def apply(form:FormDefinition,results: Seq[Property[Json]],keys:Seq[String]):TypedTag[Element] = {

    div(BootstrapStyles.row)(
      div(BootstrapStyles.Grid.colMd6)(
        for((field,i) <- form.fields.zipWithIndex) yield {
          div(
            UdashForm(
              results.lift(i).map { r =>
                fieldRenderer(field,r,keys)
              }
            ).render
          )
        }
      )
    )
  }

  def apply(schema:Option[FormDefinition],results: Seq[Property[Json]],keys:Seq[String]):TypedTag[Element] = apply(schema.getOrElse(FormDefinition(Seq())),results,keys)
}
