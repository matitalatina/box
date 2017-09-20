package ch.wsl.box.client.views.components


import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.Labels
import ch.wsl.box.model.shared._
import io.circe.Json
import ch.wsl.box.shared.utils.JsonUtils._
import io.udash.properties.single.Property
import org.scalajs.dom.{Element, Event}
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash.bootstrap.form.{UdashForm, UdashInputGroup}
import org.scalajs.dom.html.Div

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.util.Try
import scalatags.JsDom.TypedTag




/**
  * Created by andre on 4/25/2017.
  */


object JSONSchemaRenderer {


  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe._
  import io.circe.syntax._
  import scalacss.ScalatagsCss._


  final val dateTimePickerFormat = "YYYY-MM-DD hh:mm"
  final val datePickerFormat = "YYYY-MM-DD"
  final val timePickerFormat = "hh:mm"


  def numberInput(model:Property[String],labelString:Option[String] = None) = {
    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      if(labelString.exists(_.length > 0)) label(labelString) else {},
      NumberInput(model,BootstrapStyles.pullRight,textAlign.right)
    )
  }

  def textInput(model:Property[String],labelString:Option[String],xs:Modifier*) = {
    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      if(labelString.exists(_.length > 0)) label(labelString) else {},
      TextInput(model,BootstrapStyles.pullRight,xs)
    )
  }

  def textArea(model:Property[String],labelString:Option[String],xs:Modifier*) = {
    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      if(labelString.exists(_.length > 0)) label(labelString) else {},
      TextArea(model,BootstrapStyles.pullRight,xs)
    )
  }

  def datetimepicker(modelLabel:String, model:Property[Json], format:String = dateTimePickerFormat):Modifier = {


    def toDate(jsonDate:Json):Option[java.util.Date] = Try{
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

    def fromDate(dt:Option[java.util.Date]):Json = Try{
      if (!dt.isDefined)
        Json.Null
      else {
        val date = new Date(dt.get.getTime)
        if (date.getFullYear() == 1970 && date.getMonth() == 0 && date.getDate() == 1) return Json.Null
        val result = format match {
          case `dateTimePickerFormat` => date.getFullYear() + "-" + "%02d".format(date.getMonth() + 1) + "-" + "%02d".format(date.getDate()) + " " + "%02d".format(date.getHours()) + ":" + "%02d".format(date.getSeconds())
          case `datePickerFormat` => date.getFullYear() + "-" + "%02d".format(date.getMonth() + 1) + "-" + "%02d".format(date.getDate())
          case `timePickerFormat` => "%02d".format(date.getHours()) + ":" + "%02d".format(date.getMinutes())
        }
        result.asJson
      }
    }.getOrElse(Json.Null)


    val date = model.transform(toDate,fromDate)

    val pickerOptions = ModelProperty(UdashDatePicker.DatePickerOptions(
      format = format,
      locale = Some("en_GB"),
      showClear = true,
      useStrict = false
    ))
    val picker: UdashDatePicker = UdashDatePicker()(date, pickerOptions)

    div(
      if(modelLabel.length >0) label(modelLabel) else {},
      showIf(model.transform(_ != Json.Null)) {
        div(BootstrapStyles.pullRight,
          picker.render,
          a(Labels.form.removeDate, onclick :+= ((e:Event) => model.set(Json.Null)))
        ).render
      },
      showIf(model.transform(_ == Json.Null)) {
        div(a(Labels.form.addDate, onclick :+= ((e:Event) => model.set(fromDate(Some(new java.util.Date)))))).render
      }
    ).render
  }

  def optionsRenderer(modelLabel:String, options:JSONFieldOptions, model:Property[String]):Modifier = {

    def value2Label(org:String):String = options.options.find(_._1 == org).map(_._2).getOrElse("Val not found")
    def label2Value(v:String):String = options.options.find(_._2 == v).map(_._1).getOrElse("")

    val selectModel = model.transform(value2Label,label2Value)

    div(BootstrapCol.md(12),GlobalStyles.noPadding)(
      if(modelLabel.length >0) label(modelLabel) else {},
      Select(selectModel,options.options.values.toSeq,Select.defaultLabel)(BootstrapStyles.pullRight)
    )
  }

  def fixedLookupRenderer(modelLabel:String, options:JSONFieldOptions,model:Property[String]):Modifier = {
    val value:String = options.options.lift(model.get).getOrElse(model.get)
    div(
      if(modelLabel.length >0) label(modelLabel) else {},
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
    def jsonToString(json:Json):String = json.string
    def strToJson(str:String):Json = field.`type` match {
      case "number" => str.toDouble.asJson
      case _ => str.asJson
    }

    //transforma an udash property of Json to property of String
    val stringModel = model.transform[String](jsonToString(_),strToJson(_))

    (field.`type`, field.widget, field.lookup, keys.contains(field.key), field.subform) match {

      case (_,Some(WidgetsNames.hidden),_,_,_) => { }
      case (_,_,Some(options),_,_) => optionsRenderer(label,options,stringModel)
      //case (_,_,Some(options),true,_) => fixedLookupRenderer(label,options,stringModel)
      case (_,_,_,true,_) => {
        textInput(stringModel,Some(label),disabled := true, textAlign.right)
      }
      case ("number",Some(WidgetsNames.checkbox),_,_,_) => checkBox(label,model)
      case ("number",Some(WidgetsNames.nolabel),_,_,_) => numberInput(stringModel)
      case ("number",_,_,_,_) => numberInput(stringModel,Some(label))
      case ("string",Some(WidgetsNames.timepicker),_,_,_) => datetimepicker(label,model,timePickerFormat)
      case ("string",Some(WidgetsNames.datepicker),_,_,_) => datetimepicker(label,model,datePickerFormat)
      case ("string",Some(WidgetsNames.datetimePicker),_,_,_) => datetimepicker(label,model)
      case ("subform",_,_,_,Some(sub)) => subformRenderer.render(model,label,sub)
      case (_,Some(WidgetsNames.nolabel),_,_,_) => textInput(stringModel,None)
      case (_,Some(WidgetsNames.twoLines),_,_,_) => textArea(stringModel,Some(label),rows := 2)
      case (_,Some(WidgetsNames.textarea),_,_,_) => textArea(stringModel,Some(label))
      case (_,_,_,_,_) => textInput(stringModel,Some(label))
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

    def subBlock(block:SubLayoutBlock) = div(BootstrapCol.md(12),GlobalStyles.subBlock)(
          fieldsRenderer(block.fields,Stream.continually(block.fieldsWidth.toStream).flatten)
    )


    def fieldsRenderer(fields:Seq[Either[String,SubLayoutBlock]],widths:Stream[Int] = Stream.continually(12)): TypedTag[Div] = div(
      fields.zip(widths).map{ case (field,width) =>

        div(BootstrapCol.md(width),GlobalStyles.field,
          field match {
            case Left(key) => {
              for {
                result <- resultMap.toMap.lift(key)
                field <- form.fields.find(_.key == key)
              } yield {
                fieldRenderer(field, result, form.keys, subforms = subforms, subformRenderer = subformRenderer)
              }
            }.getOrElse(div())
            case Right(subForm) => subBlock(subForm)
          }
        )
      }

    )

    div(UdashForm(
      div(BootstrapStyles.row)(
        form.layout.blocks.map{ block =>
          div(BootstrapCol.md(block.width),GlobalStyles.block)(
            block.title.map{title => h3(Labels(title))},
            fieldsRenderer(block.fields)
          )
        }
      )
    ).render)
  }

}
