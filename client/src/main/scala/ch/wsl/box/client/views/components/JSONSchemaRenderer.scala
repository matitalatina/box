package ch.wsl.box.client.views.components


import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.Labels
import ch.wsl.box.client.views.components.widget._
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


class JSONSchemaRenderer(form: JSONMetadata, results: Property[Seq[(String, Json)]], subforms: Seq[JSONMetadata]) {


  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe._
  import io.circe.syntax._
  import scalacss.ScalatagsCss._


  private val key: Property[String] = Property("")
  results.listen { res =>
    val keys = res.filter(r => form.keys.contains(r._1))
    val currentKey = JSONKeys.fromMap(keys).asString
    if (currentKey != key.get) {
      key.set(currentKey)
    }
  }

  private val resultMap: Seq[(String, Property[Json])] = for ((field, i) <- form.fields.zipWithIndex) yield {
    field.key -> results.transform(seqJsonToJson(i), jsonToSeqJson(i, field.key))
  }
  private val subformRenderer = SubformRenderer(results.get, subforms)

  private def widgetSelector(field: JSONField): Widget = {


    (field.`type`, field.widget, field.lookup, form.keys.contains(field.key), field.subform) match {
      case (_, Some(WidgetsNames.hidden), _, _, _) => HiddenWidget
      case (_, Some(WidgetsNames.popup), Some(options), _, _) => PopupWidget(options)
      case (_, _, Some(options), _, _) => SelectWidget(options, field)
      case (_, _, _, true, _) => InputWidget(disabled := true, textAlign.right).Text
      case ("number", Some(WidgetsNames.checkbox), _, _, _) => CheckboxWidget
      case ("number", Some(WidgetsNames.nolabel), _, _, _) => InputWidget.noLabel().Number
      case ("number", _, _, _, _) => InputWidget().Number
      case ("string", Some(WidgetsNames.timepicker), _, _, _) => DateTimeWidget.Time
      case ("string", Some(WidgetsNames.datepicker), _, _, _) => DateTimeWidget.Date
      case ("string", Some(WidgetsNames.datetimePicker), _, _, _) => DateTimeWidget.DateTime
      case ("subform", _, _, _, Some(sub)) => subformRenderer.SubformWidget(sub)
      case (_, Some(WidgetsNames.nolabel), _, _, _) => InputWidget.noLabel().Text
      case (_, Some(WidgetsNames.twoLines), _, _, _) => InputWidget(rows := 2).Textarea
      case (_, Some(WidgetsNames.textarea), _, _, _) => InputWidget().Textarea
      case (_, _, _, _, _) => InputWidget().Text
    }

  }




  private def seqJsonToJson(i: Int)(seq: Seq[(String, Json)]): Json = seq.lift(i).map(_._2).getOrElse(Json.Null)

  private def jsonToSeqJson(i: Int, key: String)(n: Json): Seq[(String, Json)] = for {
    (e, j) <- results.get.zipWithIndex
  } yield {
    if (i == j) key -> n else e
  }



  private def subBlock(block: SubLayoutBlock) = div(BootstrapCol.md(12), GlobalStyles.subBlock)(
    fieldsRenderer(key, block.fields, Stream.continually(block.fieldsWidth.toStream).flatten)
  )

  private def simpleField(fieldKey:String) = {for{
    result <- resultMap.toMap.lift(fieldKey)
    field <- form.fields.find(_.key == fieldKey)
  } yield {
    widgetSelector(field).render(key,field.title.getOrElse(field.key),result)
  }}.getOrElse(div())


  private def fieldsRenderer(keyProp: Property[String], fields: Seq[Either[String, SubLayoutBlock]], widths: Stream[Int] = Stream.continually(12)): TypedTag[Div] = div(
    fields.zip(widths).map { case (field, width) =>
      div(BootstrapCol.md(width), GlobalStyles.field,
        field match {
          case Left(fieldKey) => simpleField(fieldKey)
          case Right(subForm) => subBlock(subForm)
        }
      )
    }

  )


  def render() = {
    div(UdashForm(
      div(BootstrapStyles.row)(
        form.layout.blocks.map { block =>
          div(BootstrapCol.md(block.width), GlobalStyles.block)(
            block.title.map { title => h3(Labels(title)) },
            fieldsRenderer(key, block.fields)
          )
        }
      )
    ).render)
  }

}

object JSONSchemaRenderer {
  def apply(form: JSONMetadata, results: Property[Seq[(String, Json)]], subforms: Seq[JSONMetadata]): TypedTag[Element] = new JSONSchemaRenderer(form, results, subforms).render()
}
