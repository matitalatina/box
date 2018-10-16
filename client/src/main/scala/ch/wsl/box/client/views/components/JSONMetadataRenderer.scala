package ch.wsl.box.client.views.components


import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.{Conf, Labels}
import ch.wsl.box.client.views.components.widget._
import ch.wsl.box.model.shared._
import ch.wsl.box.shared.utils.JsonUtils._
import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.form.UdashForm
import io.udash._
import io.udash.bootstrap.tooltip.UdashTooltip

import scala.concurrent.Future
import scalatags.JsDom
import scalatags.JsDom.all._
import io.udash.bindings.modifiers.Binding

import scalacss.ScalatagsCss._
import io.udash.css.CssView._
/**
  * Created by andre on 4/25/2017.
  */


case class JSONMetadataRenderer(metadata: JSONMetadata, data: Property[Json], children: Seq[JSONMetadata]) extends WidgetBinded {


  import ch.wsl.box.client.Context._
  import io.circe._

  import scalacss.ScalatagsCss._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._


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

  private def checkCondition(fieldData: ReadableProperty[Json],field: JSONField) = {
    field.condition match {
      case None => Property(true)
      case Some(condition) => {

        def evaluate(d:Json):Boolean = {
          val value = d.js(condition.conditionFieldId)
          val r = condition.conditionValues.contains(value)
          logger.info(s"evaluating condition for field: ${field.name} against $value with accepted values: ${condition.conditionValues} with result: $r")
          r
        }


        val visibility = Property(evaluate(data.get))
        fieldData.listen{d =>
          val r = evaluate(d)
          if(r == !visibility.get) { //change only when the status changes
            visibility.set(r)
          }
        }
        visibility
      }
    }
  }


  private def widgetSelector(field: JSONField, id:Property[String], fieldData:Property[Json]): Widget = {
    import JSONFieldTypes._


    val label = field.label.getOrElse(field.name)

    val widg =

        (field.`type`, field.widget, field.lookup, metadata.keys.contains(field.name), field.child) match {
          case (_, Some(WidgetsNames.hidden), _, _, _)                => HiddenWidget
          case (_, Some(WidgetsNames.fullWidth), Some(options), _, _) => SelectWidget(options,field,label,fieldData,width := 100.pct, toNullable(field.nullable))
          case (_, Some(WidgetsNames.popup), Some(options), _, _)     => PopupWidget(options,label,fieldData)
          case (_, _, Some(lookup), _, _)                             => SelectWidget(lookup, field,label,fieldData, toNullable(field.nullable))
          case (_, _, _, true, _)                                     => InputWidget(disabled := Conf.manualEditKeyFields, textAlign.right, toPlaceholder(field.placeholder), toNullable(field.nullable)).Text(label,fieldData)
          case (NUMBER, Some(WidgetsNames.checkbox), _, _, _)         => CheckboxWidget(label,fieldData)
          case (NUMBER, Some(WidgetsNames.nolabel), _, _, _)          => InputWidget.noLabel(toPlaceholder(field.placeholder), toNullable(field.nullable)).Number(label,fieldData)
          case (NUMBER, _, _, _, _)                                   => InputWidget(toPlaceholder(field.placeholder), toNullable(field.nullable)).Number(label,fieldData)
          case (TIME, Some(WidgetsNames.timepicker), _, _, _)         => DateTimeWidget.Time(id,label,fieldData)
          case (DATE, Some(WidgetsNames.datepicker), _, _, _)         => DateTimeWidget.Date(id,label,fieldData)
          case (DATETIME, Some(WidgetsNames.datetimePicker), _, _, _) => DateTimeWidget.DateTime(id,label,fieldData)
          case (TIME, Some(WidgetsNames.timepickerFullWidth), _, _, _) => DateTimeWidget.TimeFullWidth(id,label,fieldData)
          case (DATE, Some(WidgetsNames.datepickerFullWidth), _, _, _) => DateTimeWidget.DateFullWidth(id,label,fieldData)
          case (DATETIME, Some(WidgetsNames.datetimePickerFullWidth), _, _, _) => DateTimeWidget.DateTimeFullWidth(id,label,fieldData)
          case (CHILD, _, _, _, Some(child))                          => ChildRenderer(child,children,fieldData,dataWithChildId)
          case (_, Some(WidgetsNames.nolabel), _, _, _)               => InputWidget.noLabel(toPlaceholder(field.placeholder), toNullable(field.nullable)).Text(label,fieldData)
          case (_, Some(WidgetsNames.twoLines), _, _, _)              => InputWidget(rows := 2,toPlaceholder(field.placeholder), toNullable(field.nullable)).Textarea(label,fieldData)
          case (_, Some(WidgetsNames.textarea), _, _, _)              => InputWidget(toPlaceholder(field.placeholder), toNullable(field.nullable)).Textarea(label,fieldData)
          case (FILE, _, _, _, _)                                     => FileWidget(id,fieldData,field,label,metadata.entity)
          case (_, _, _, _, _)                                        => InputWidget(toPlaceholder(field.placeholder), toNullable(field.nullable)).Text(label,fieldData)
    }

    widg

  }


  private def toPlaceholder(placeHolder: Option[String]):Seq[Modifier]={
    placeHolder.getOrElse("") match{
      case "" => Seq.empty
      case p => Seq(placeholder := p)
    }
  }

  private def toNullable(nullable: Boolean):Seq[Modifier]={
    nullable match{
      case true => Seq.empty
      case false => Seq(GlobalStyles.notNullable)
    }
  }


  case class WidgetVisibility(widget:Widget,visibility: ReadableProperty[Boolean])
  object WidgetVisibility{
    def apply(widget: Widget): WidgetVisibility = WidgetVisibility(widget, Property(true))
  }

  private def subBlock(block: SubLayoutBlock):WidgetVisibility = WidgetVisibility(new Widget {

    val widget = fieldsRenderer(block.fields, Stream.continually(block.fieldsWidth.toStream).flatten)

    override def afterSave(data:Json,form:JSONMetadata): Future[Unit] = widget.afterSave(data,form)
    override def beforeSave(data:Json,form:JSONMetadata): Future[Unit] = widget.beforeSave(data,form)

    override def killWidget(): Unit = widget.killWidget()

    override protected def show(): JsDom.all.Modifier = render(false)

    override protected def edit(): JsDom.all.Modifier = render(true)

    private def render(write:Boolean): JsDom.all.Modifier = div(BootstrapCol.md(12), GlobalStyles.subBlock)(
      widget.render(write,Property(true))
    )
  })

  private def simpleField(fieldName:String):WidgetVisibility = {for{
    field <- metadata.fields.find(_.name == fieldName)
  } yield {



    val fieldData = Property(dataWithChildId.get.js(field.name))


    dataWithChildId.listen{ d =>
      val newJs = d.js(field.name)
      if( newJs != fieldData.get) {
        fieldData.set(newJs)
      }
    }

    fieldData.listen{ fd =>
      if(dataWithChildId.get.js(field.name) != fd) {
        dataWithChildId.set(dataWithChildId.get.deepMerge(Json.obj((field.name,fd))))
      }
    }

    WidgetVisibility(widgetSelector(field, id, fieldData),checkCondition(fieldData,field))

  }}.getOrElse(WidgetVisibility(HiddenWidget))


  private def fieldsRenderer(fields: Seq[Either[String, SubLayoutBlock]], widths: Stream[Int] = Stream.continually(12)):Widget = new Widget {

    val widgets:Seq[WidgetVisibility] = fields.map{
      case Left(fieldName) => simpleField(fieldName)
      case Right(subForm) => subBlock(subForm)
    }

    override def afterSave(value:Json,metadata:JSONMetadata): Future[Unit] = afterSaveAll(value,metadata,widgets.map(_.widget))
    override def beforeSave(value:Json,metadata:JSONMetadata): Future[Unit] = beforeSaveAll(value,metadata,widgets.map(_.widget))

    override def killWidget(): Unit = widgets.foreach(_.widget.killWidget())

    override protected def show(): JsDom.all.Modifier = render(false)

    override protected def edit(): JsDom.all.Modifier = render(true)

    private def render(write:Boolean): JsDom.all.Modifier = div(
      widgets.zip(widths).map { case (widget, width) =>
        div(BootstrapCol.md(width), GlobalStyles.field,
          widget.widget.render(write,widget.visibility)
        )
      }

    )
  }




    val blocks = metadata.layout.blocks.map { block =>
      (
        block,
        fieldsRenderer(block.fields)
      )
    }

    override def afterSave(value:Json, metadata:JSONMetadata): Future[Unit] = afterSaveAll(value,metadata,blocks.map(_._2))
    override def beforeSave(value:Json, metadata:JSONMetadata): Future[Unit] = beforeSaveAll(value,metadata,blocks.map(_._2))

  override def killWidget(): Unit = blocks.foreach(_._2.killWidget())


  override protected def show(): JsDom.all.Modifier = render(false)

  override def edit(): JsDom.all.Modifier = render(true)

  import io.udash._


  private def render(write:Boolean): JsDom.all.Modifier = {
    def renderer(block: LayoutBlock, widget:Widget) = {
      div(
        h3(block.title.map { title => Labels(title) }),
        widget.render(write, Property {
          true
        })
      ).render
    }

    div(UdashForm(
      Debug(data,autoRelease, "data"),
      div(BootstrapStyles.row)(
        blocks.map{ case (block,widget) =>
          div(BootstrapCol.md(block.width), GlobalStyles.block)(
            renderer(block,widget)
          )
        }
      )
    ).render)
  }


}
