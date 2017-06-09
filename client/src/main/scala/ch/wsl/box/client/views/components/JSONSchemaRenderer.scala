package ch.wsl.box.client.views.components


import ch.wsl.box.client.styles.BootstrapCol
import ch.wsl.box.client.utils.Labels
import ch.wsl.box.model.shared._
import io.circe.Json
import ch.wsl.box.shared.utils.JsonUtils._
import io.udash.properties.single.Property
import org.scalajs.dom.Element
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash.bootstrap.form.{UdashForm, UdashInputGroup}
import org.scalajs.dom.html.Div

import scala.scalajs.js
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


  final val dateTimePickerFormat = "YYYY-MM-DD hh:mm"
  final val datePickerFormat = "YYYY-MM-DD"
  final val timePickerFormat = "hh:mm"

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
          val year = 1970
          val month = 1
          val day = 1
          val timeTokens = str.split(":")
          val hours =  timeTokens(0).toInt
          val minutes = timeTokens(1).toInt
          new java.util.Date(year-1900,month,day,hours,minutes)
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
          val result = new java.util.Date(year-1900,month,day,hours,minutes)
          println(s"original $str, result: $result")
          result
        }
        case `datePickerFormat` => {
          val dateTokens = str.split("-")
          val year = dateTokens(0).toInt
          val month = dateTokens(1).toInt
          val day = dateTokens(2).toInt
          val result = new java.util.Date(year-1900,month,day)
          println(s"original $str, result: $result")
          result
        }
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

    def value2Label(org:String):String = options.options.find(_._1 == org).map(_._2).getOrElse("Val not found")
    def label2Value(v:String):String = options.options.find(_._2 == v).map(_._1).getOrElse("")

    val selectModel = model.transform(value2Label,label2Value)

    div(BootstrapStyles.Form.formGroup)(
      label(modelLabel),
      Select(selectModel,options.options.values.toSeq,BootstrapStyles.Form.formControl)
    )
  }

  def fixedLookupRenderer(modelLabel:String, options:JSONFieldOptions,model:Property[String]):Modifier = {
    val value:String = options.options.lift(model.get).getOrElse(model.get)
    div(
      label(modelLabel),
      br,
      value
    )
  }


  def checkBox(label:String,model:Property[Json]) = {
    def jsToBool(json:Json):Boolean = json.asNumber.flatMap(_.toInt).exists(_ == 1)
    def boolToJson(v:Boolean):Json = v match {
      case true => 1.asJson
      case false => 0.asJson
    }
    val booleanModel = model.transform[Boolean](jsToBool(_),boolToJson(_))
    div(
      Checkbox(booleanModel), " ", label
    )
  }

  def fieldRenderer(field:JSONField, model:Property[Json], keys:Seq[String], showLabel:Boolean = true, subforms:Seq[JSONMetadata] = Seq(), subformRenderer: SubformRenderer):Modifier = {
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
      case (_,Some(WidgetsNames.hidden),_,_,_) => { }
      case (_,_,Some(options),_,_) => optionsRenderer(label,options,stringModel)
      //case (_,_,Some(options),true,_) => fixedLookupRenderer(label,options,stringModel)
      case (_,_,_,true,_) => {
        UdashForm.textInput()(label)(stringModel,disabled := true)
      }
      case ("number",Some(WidgetsNames.checkbox),_,_,_) => checkBox(label,model)
      case ("number",Some(WidgetsNames.nolabel),_,_,_) => UdashForm.numberInput()()(stringModel)
      case ("number",_,_,_,_) => UdashForm.numberInput()(label)(stringModel)
      case ("string",Some(WidgetsNames.timepicker),_,_,_) => datetimepicker(label,stringModel,timePickerFormat)
      case ("string",Some(WidgetsNames.datepicker),_,_,_) => datetimepicker(label,stringModel,datePickerFormat)
      case ("string",Some(WidgetsNames.datetimePicker),_,_,_) => datetimepicker(label,stringModel)
      case ("subform",_,_,_,Some(sub)) => subformRenderer.render(model,label,sub)
      case (_,Some(WidgetsNames.nolabel),_,_,_) => UdashForm.textInput()()(stringModel)
      case (_,_,_,_,_) => UdashForm.textInput()(label)(stringModel)
    }
  }

  def apply(form:JSONMetadata, results: Property[Seq[(String,Json)]], subforms:Seq[JSONMetadata]):TypedTag[Element] = {

    def seqJsonToJson(i:Int)(seq:Seq[(String,Json)]):Json = seq.lift(i).map(_._2).getOrElse(Json.Null)
    def jsonToSeqJson(i:Int,key:String)(n:Json):Seq[(String,Json)] = for{
      (e,j) <- results.get.zipWithIndex
    } yield {
      if(i == j) key -> n else e
    }

    val resultMap:Seq[(String,Property[Json])] = for((field,i) <- form.fields.zipWithIndex) yield {
      field.key -> results.transform(seqJsonToJson(i),jsonToSeqJson(i,field.key))
    }

    val subformRenderer = SubformRenderer(results.get,subforms)


    def blockRenderer(block:LayoutBlock) = (block.fields,block.subBlock) match {
      case (Some(f),None) => fieldsRenderer(f)
      case (None,Some(sub)) => subBlock(sub)
      case (_,_) => throw new Exception(s"Invalid layout $block")
    }

    def subBlock(sub:Seq[SubLayoutBlock]) = div(BootstrapStyles.row)(
      sub.map{ block =>
        div(BootstrapCol.md(block.width))(
          fieldsRenderer(block.fields)
        )
      }
    )

    def fieldsRenderer(fields:Seq[String]) = div(
      for{
        key <- fields
        result <- resultMap.toMap.lift(key)
        field <- form.fields.find(_.key == key)
      } yield {
        fieldRenderer(field,result,form.keys, subforms = subforms, subformRenderer = subformRenderer)
      }
    )

    div(UdashForm(
      div(BootstrapStyles.row)(
        form.layout.blocks.map{ block =>
          div(BootstrapCol.md(block.width))(
            block.title.map{title => h3(Labels(title))},
            blockRenderer(block)
          )
        }
      )
    ).render)
  }

}
