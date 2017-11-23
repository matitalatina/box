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

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.util.Try
import scalatags.JsDom
import scalatags.JsDom.TypedTag


/**
  * Created by andre on 4/25/2017.
  */


case class JSONSchemaRenderer(form: JSONMetadata, results: Property[Json], subforms: Seq[JSONMetadata]) {


  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe._
  import io.circe.syntax._
  import scalacss.ScalatagsCss._

  private def keysFromResult(res:Json) = {
    res.keys(form.keys).asString
  }

  private val key: Property[String] = Property(keysFromResult(results.get))
  results.listen { res =>
    val currentKey = keysFromResult(res)
    if (currentKey != key.get) {
      key.set(currentKey)
    }
  }


  private val subformRenderer = SubformRenderer(results, subforms)

  private def widgetSelector(key:Property[String],result:Property[Json],field: JSONField): Widget = {

    val label = field.title.getOrElse(field.key)

    (field.`type`, field.widget, field.lookup, form.keys.contains(field.key), field.subform) match {
      case (_, Some(WidgetsNames.hidden), _, _, _) => HiddenWidget
      case (_, Some(WidgetsNames.popup), Some(options), _, _) => PopupWidget(options,label,result)
      case (_, _, Some(options), _, _) => SelectWidget(options, field,label,result)
      case (_, _, _, true, _) => InputWidget(disabled := true, textAlign.right).Text(label,result)
      case ("number", Some(WidgetsNames.checkbox), _, _, _) => CheckboxWidget(label,result)
      case ("number", Some(WidgetsNames.nolabel), _, _, _) => InputWidget.noLabel().Number(label,result)
      case ("number", _, _, _, _) => InputWidget().Number(label,result)
      case ("string", Some(WidgetsNames.timepicker), _, _, _) => DateTimeWidget.Time(key,label,result)
      case ("string", Some(WidgetsNames.datepicker), _, _, _) => DateTimeWidget.Date(key,label,result)
      case ("string", Some(WidgetsNames.datetimePicker), _, _, _) => DateTimeWidget.DateTime(key,label,result)
      case ("subform", _, _, _, Some(sub)) => subformRenderer.SubformWidget(sub,result)
      case (_, Some(WidgetsNames.nolabel), _, _, _) => InputWidget.noLabel().Text(label,result)
      case (_, Some(WidgetsNames.twoLines), _, _, _) => InputWidget(rows := 2).Textarea(label,result)
      case (_, Some(WidgetsNames.textarea), _, _, _) => InputWidget().Textarea(label,result)
      case ("file", _, _, _, _) => FileWidget(key,result,field,label)
      case (_, _, _, _, _) => InputWidget().Text(label,result)
    }

  }


  private def subBlock(block: SubLayoutBlock):Widget = new Widget {

    val widget = fieldsRenderer(key, block.fields, Stream.continually(block.fieldsWidth.toStream).flatten)

    override def afterSave(result:Json,form:JSONMetadata): Future[Unit] = widget.afterSave(result,form)
    override def beforeSave(result:Json,form:JSONMetadata): Future[Unit] = widget.beforeSave(result,form)

    override def render(): JsDom.all.Modifier = div(BootstrapCol.md(12), GlobalStyles.subBlock)(
      widget.render()
    )
  }

  private def simpleField(fieldKey:String):Widget = {for{
    field <- form.fields.find(_.key == fieldKey)
  } yield {

    def toSingle(all:Json):Json = {
      val result = all.js(field.key)
      //println(s"all: $all \n\n result: $result")
      result
    }
    def toAll(single:Json):Json = results.get.deepMerge(Json.obj((field.key,single)))
    //results.listen(js => println("result changed"))

    widgetSelector(key,results.transform(toSingle,toAll),field)

  }}.getOrElse(HiddenWidget)


  private def fieldsRenderer(keyProp: Property[String], fields: Seq[Either[String, SubLayoutBlock]], widths: Stream[Int] = Stream.continually(12)):Widget = new Widget {

    val widgets:Seq[Widget] = fields.map{
      case Left(fieldKey) => simpleField(fieldKey)
      case Right(subForm) => subBlock(subForm)
    }

    override def afterSave(result:Json,form:JSONMetadata): Future[Unit] = afterSaveAll(result,form,widgets)
    override def beforeSave(result:Json,form:JSONMetadata): Future[Unit] = beforeSaveAll(result,form,widgets)

    override def render(): JsDom.all.Modifier = div(
      widgets.zip(widths).map { case (widget, width) =>
        div(BootstrapCol.md(width), GlobalStyles.field,
          widget.render()
        )
      }

    )
  }


  def widget():Widget = new Widget {

    val blocks = form.layout.blocks.map { block =>
      (
        block.width,
        block.title,
        fieldsRenderer(key, block.fields)
      )
    }

    override def afterSave(result:Json,form:JSONMetadata): Future[Unit] = afterSaveAll(result,form,blocks.map(_._3))
    override def beforeSave(result:Json,form:JSONMetadata): Future[Unit] = beforeSaveAll(result,form,blocks.map(_._3))

    override def render(): JsDom.all.Modifier = div(UdashForm(
      div(BootstrapStyles.row)(
        blocks.map{ case (width,title,widget) =>
          div(BootstrapCol.md(width), GlobalStyles.block)(
            title.map{ title => h3(Labels(title)) },
            widget.render()
          )
        }
      )
    ).render)
  }

}
