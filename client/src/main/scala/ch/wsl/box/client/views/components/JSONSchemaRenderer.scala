package ch.wsl.box.client.views.components

import ch.wsl.box.model.shared.{JSONField, JSONSchema}
import io.udash.properties.single.Property
import org.scalajs.dom.Element
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.datepicker.UdashDatePicker
import io.udash.bootstrap.form.{UdashForm, UdashInputGroup}

import scalatags.JsDom.TypedTag


/**
  * Created by andre on 4/25/2017.
  */
object JSONSchemaRenderer {

  case class FormDefinition(schema:JSONSchema,fields:Seq[JSONField])

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._


  def datepicker(modelLabel:String, model:Property[String]):Modifier = {
    val pickerOptions = ModelProperty(UdashDatePicker.DatePickerOptions(
      format = "MMMM Do YYYY, hh:mm a",
      locale = Some("en_GB")
    ))


    def toDate(str:String):java.util.Date = new java.util.Date()
    def fromDate(dt:java.util.Date):String = ""
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


  def fieldRenderer(field:JSONField,model:Property[String]):Modifier = {
    val label = field.title.getOrElse(field.key)
    (field.`type`,field.widget,field.options) match {
      case ("number",_,_) => UdashForm.numberInput()(label)(model)
      case ("string",Some("timepicker"),_) => datepicker(label,model)
      case ("string",Some("datepicker"),_) => UdashForm.textInput()(label)(model)
      case ("string",Some("datetimepicker"),_) => UdashForm.textInput()(label)(model)
      case (_,_,_) => UdashForm.textInput()(label)(model)
    }
  }

  def apply(form:FormDefinition,results: Seq[Property[String]]):TypedTag[Element] = {

    div(BootstrapStyles.row)(
      div(BootstrapStyles.Grid.colMd6)(
        for((((name,props),field),i) <- form.schema.properties.toSeq.zip(form.fields).zipWithIndex) yield {
          div(
            UdashForm(
              results.lift(i).map { r =>
                fieldRenderer(field,r)
              }
            ).render
          )
        }
      )
    )
  }

  def apply(schema:Option[FormDefinition],results: Seq[Property[String]]):TypedTag[Element] = apply(schema.getOrElse(FormDefinition(JSONSchema.empty,Seq())),results)
}
