package ch.wsl.box.client.views.components


import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.{Conf, Labels}
import ch.wsl.box.client.views.components.widget._
import ch.wsl.box.model.shared._
import ch.wsl.box.shared.utils.JsonUtils._
import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.form.UdashForm
import io.udash.properties.single.Property

import scala.concurrent.Future
import scalatags.JsDom

/**
  * Created by andre on 4/25/2017.
  */


case class JSONMetadataRenderer(metadata: JSONMetadata, data: Property[Json], children: Seq[JSONMetadata]) extends WidgetBinded {


  import ch.wsl.box.client.Context._
  import io.circe._

  import scalacss.ScalatagsCss._
  import scalatags.JsDom.all._


  private def getId(data:Json): String = {
    data.ID(metadata.keys).asString
  }

  private val id: Property[String] = Property(getId(dataWithChildId.get))

  dataWithChildId.listen { data =>
    val currentID = getId(data)
    if (currentID != id.get) {
      id.set(currentID)
    }
  }



  private def widgetSelector(field: JSONField, id:Property[String], data:Property[Json]): Widget = {
    import JSONFieldTypes._

    val label = field.label.getOrElse(field.name)

    (field.`type`, field.widget, field.lookup, metadata.keys.contains(field.name), field.child) match {
      case (_, Some(WidgetsNames.hidden), _, _, _) => HiddenWidget
      case (_, Some(WidgetsNames.fullWidth), Some(options), _, _) => SelectWidget(options,field,label,data,width := 100.pct)
      case (_, Some(WidgetsNames.popup), Some(options), _, _) => PopupWidget(options,label,data)
      case (_, _, Some(lookup), _, _) => SelectWidget(lookup, field,label,data)
      case (_, _, _, true, _) => InputWidget(disabled := Conf.manualEditKeyFields, textAlign.right).Text(label,data)
      case (NUMBER, Some(WidgetsNames.checkbox), _, _, _) => CheckboxWidget(label,data)
      case (NUMBER, Some(WidgetsNames.nolabel), _, _, _) => InputWidget.noLabel().Number(label,data)
      case (NUMBER, _, _, _, _) => InputWidget().Number(label,data)
      case (STRING, Some(WidgetsNames.timepicker), _, _, _) => DateTimeWidget.Time(id,label,data)
      case (STRING, Some(WidgetsNames.datepicker), _, _, _) => DateTimeWidget.Date(id,label,data)
      case (STRING, Some(WidgetsNames.datetimePicker), _, _, _) => DateTimeWidget.DateTime(id,label,data)
      case (CHILD, _, _, _, Some(child)) => ChildRenderer(child,children,data,dataWithChildId)
      case (_, Some(WidgetsNames.nolabel), _, _, _) => InputWidget.noLabel().Text(label,data)
      case (_, Some(WidgetsNames.twoLines), _, _, _) => InputWidget(rows := 2).Textarea(label,data)
      case (_, Some(WidgetsNames.textarea), _, _, _) => InputWidget().Textarea(label,data)
      case (FILE, _, _, _, _) => FileWidget(id,data,field,label,metadata.entity)
      case (_, _, _, _, _) => InputWidget().Text(label,data)
    }

  }


  private def subBlock(block: SubLayoutBlock):Widget = new Widget {

    val widget = fieldsRenderer(block.fields, Stream.continually(block.fieldsWidth.toStream).flatten)

    override def afterSave(data:Json,form:JSONMetadata): Future[Unit] = widget.afterSave(data,form)
    override def beforeSave(data:Json,form:JSONMetadata): Future[Unit] = widget.beforeSave(data,form)

    override def render(): JsDom.all.Modifier = div(BootstrapCol.md(12), GlobalStyles.subBlock)(
      widget.render()
    )
  }

  private def simpleField(fieldName:String):Widget = {for{
    field <- metadata.fields.find(_.name == fieldName)
  } yield {

    def toSingle(all:Json):Json = {
      val result = all.js(field.name)
      //println(s"all: $all \n\n record: $record")
      result
    }
    def toAll(single:Json):Json = dataWithChildId.get.deepMerge(Json.obj((field.name,single)))
    //results.listen(js => println("record changed"))

    widgetSelector(field, id, dataWithChildId.transform(toSingle,toAll))

  }}.getOrElse(HiddenWidget)


  private def fieldsRenderer(fields: Seq[Either[String, SubLayoutBlock]], widths: Stream[Int] = Stream.continually(12)):Widget = new Widget {

    val widgets:Seq[Widget] = fields.map{
      case Left(fieldName) => simpleField(fieldName)
      case Right(subForm) => subBlock(subForm)
    }

    override def afterSave(value:Json,metadata:JSONMetadata): Future[Unit] = afterSaveAll(value,metadata,widgets)
    override def beforeSave(value:Json,metadata:JSONMetadata): Future[Unit] = beforeSaveAll(value,metadata,widgets)

    override def render(): JsDom.all.Modifier = div(
      widgets.zip(widths).map { case (widget, width) =>
        div(BootstrapCol.md(width), GlobalStyles.field,
          widget.render()
        )
      }

    )
  }



    val blocks = metadata.layout.blocks.map { block =>
      (
        block.width,
        block.title,
        fieldsRenderer(block.fields)
      )
    }

    override def afterSave(value:Json, metadata:JSONMetadata): Future[Unit] = afterSaveAll(value,metadata,blocks.map(_._3))
    override def beforeSave(value:Json, metadata:JSONMetadata): Future[Unit] = beforeSaveAll(value,metadata,blocks.map(_._3))

    override def render(): JsDom.all.Modifier = div(UdashForm(
      Debug(data, "data"),
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
