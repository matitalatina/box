package ch.wsl.box.client.views.components


import ch.wsl.box.model.shared._
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


  def fieldRenderer(field:JSONField,model:Property[Json],keys:Seq[String],showLabel:Boolean = true, subforms:Seq[JSONForm] = Seq()):Modifier = {
    val label = showLabel match {
      case true => field.title.getOrElse(field.key)
      case false => ""
    }
    def jsToString(json:Json):String = json.string
    def strToJson(str:String):Json = field.`type` match {
      case "number" => str.toDouble.asJson
      case _ => str.asJson
    }
    val stringModel = model.transform[String](jsToString(_),strToJson(_))
    (field.`type`,field.widget,field.options,keys.contains(field.key),field.subform) match {
      case (_,_,_,true,_) => {
        println(s"$label ${stringModel.get}")
        UdashForm.textInput()(label)(stringModel,disabled := true)
      }
      case (_,_,Some(options),_,_) => optionsRenderer(label,options,stringModel)
      case ("number",_,_,_,_) => UdashForm.numberInput()(label)(stringModel)
      case ("string",Some(WidgetsNames.timepicker),_,_,_) => datetimepicker(label,stringModel,timePickerFormat)
      case ("string",Some(WidgetsNames.datepicker),_,_,_) => datetimepicker(label,stringModel,datePickerFormat)
      case ("string",Some(WidgetsNames.datetimePicker),_,_,_) => datetimepicker(label,stringModel)
      case ("subform",_,_,_,Some(sub)) => subform(model,label,sub,subforms)
      case (_,_,_,_,_) => UdashForm.textInput()(label)(stringModel)
    }
  }

  def subform(result:Property[Json],label:String,subform:Subform,subforms:Seq[JSONForm]):Modifier = {
    def splitJson(js:Json):Seq[Json] = js.as[Seq[Json]].right.getOrElse(Seq())
    def mergeJson(longJs:Seq[Json]):Json = longJs.asJson

    def splitJsonFields(form:JSONForm)(js:Json):Seq[Json] = form.fields.map{ field =>
      js.hcursor.get[Json](field.key).right.get
    }
    def mergeJsonFields(form:JSONForm)(longJs:Seq[Json]):Json = {
      println("mergeJsonFields")
      form.fields.map(_.key).zip(longJs).toMap.asJson
    }

    subforms.find(_.id == subform.id) match {
      case None => p("subform not found")
      case Some(f) => {
        val model = result.transformToSeq(splitJson,mergeJson).elemProperties.map(_.transformToSeq(splitJsonFields(f),mergeJsonFields(f)).elemProperties)
        div(
          h4(f.name),
          for{ results <- model} yield {
            println(s"rendering form: $f with results: $results, original: ${result.get}")
            apply(f,results,subforms).render
          }.render
        )
      }
    }

  }

  def apply(form:JSONForm,results: Seq[Property[Json]],subforms:Seq[JSONForm]):TypedTag[Element] = {

    div(BootstrapStyles.row)(
      div(BootstrapStyles.Grid.colMd6)(
        for((field,i) <- form.fields.zipWithIndex) yield {
          div(
            UdashForm(
              results.lift(i).map { r =>
                fieldRenderer(field,r,form.keys, subforms = subforms)
              }
            ).render
          )
        }
      )
    )
  }

}
