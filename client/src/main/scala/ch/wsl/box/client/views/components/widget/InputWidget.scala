package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.Conf
import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes, WidgetsNames}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import scalatags.JsDom

import scala.concurrent.Future
import scalatags.JsDom.all._
import ch.wsl.box.shared.utils.JsonUtils._
import io.udash.bindings.modifiers.Binding

object InputWidgetFactory {

  object Text extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = new InputWidget.Text(field, prop)
  }

  object TextDisabled extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = new InputWidget.TextDisabled(field, prop)
  }

  object TextNoLabel extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = new InputWidget.TextNoLabel(field, prop)
  }

  object TextArea extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = new InputWidget.Textarea(field, prop)

  }

  object TwoLines extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = new InputWidget.TwoLines(field, prop)

  }

  object Number extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = new InputWidget.Number(field, prop)

  }


  object NumberNoLabel extends ComponentWidgetFactory {
    override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = new InputWidget.NumberNoLabel(field, prop)

  }

}

object InputWidget {


  import scalacss.ScalatagsCss._
  import io.udash.css.CssView._


  private def showInput(prop:Property[Json],labelString:Option[String],modifiers:Seq[Modifier] = Seq()):Binding = WidgetUtils.showNotNull(prop){ p =>

    val inputRendererDefaultModifiers:Seq[Modifier] = Seq(BootstrapStyles.pullRight)

    def withLabel = labelString.exists(_.length > 0)

//    def withTooltip = hasTooltip && labelString.length > 0

    div(BootstrapCol.md(12),GlobalStyles.noPadding,GlobalStyles.smallBottomMargin,
      if(withLabel) label(labelString) else {},
      if(withLabel)
        div(inputRendererDefaultModifiers++modifiers, bind(prop.transform(_.string)))
      else
        div(inputRendererDefaultModifiers++modifiers++Seq(width := 100.pct), bind(prop.transform(_.string))),
      div(BootstrapStyles.Visibility.clearfix)
    ).render

  }

  private def input(field:JSONField, withLabel:Boolean,modifiers:Seq[Modifier] = Seq())(inputRenderer:(Seq[Modifier]) => Modifier):Modifier = {

    val inputRendererDefaultModifiers:Seq[Modifier] = Seq(BootstrapStyles.pullRight)


    val ph = field.placeholder match{
      case Some(p) if p.nonEmpty => Seq(placeholder := p)
      case _ => Seq.empty
    }



    val allModifiers = inputRendererDefaultModifiers++ph++ WidgetUtils.toNullable(field.nullable) ++modifiers

    div(BootstrapCol.md(12),GlobalStyles.noPadding,GlobalStyles.smallBottomMargin,
      if(withLabel) WidgetUtils.toLabel(field) else {},
      if(withLabel)
        inputRenderer(allModifiers)
      else
        inputRenderer(allModifiers++Seq(width := 100.pct)),
      div(BootstrapStyles.Visibility.clearfix)
    )

  }

  class Text(val field:JSONField, prop: Property[Json]) extends Widget {

    val modifiers:Seq[Modifier] = Seq()


    override def edit() = input(field,true,modifiers){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextInput.apply(stringModel,None,y:_*)
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,field.label,modifiers))
  }

  class TextDisabled(field:JSONField, prop: Property[Json]) extends Text(field,prop) {
    override val modifiers = Seq(disabled := Conf.manualEditKeyFields, textAlign.right)
  }

  case class TextNoLabel(field:JSONField, prop: Property[Json]) extends Widget {


    override def edit() = input(field,false){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextInput.apply(stringModel,None,y:_*)
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,None))
  }

  class Textarea(val field:JSONField, prop: Property[Json]) extends Widget {

    val modifiers:Seq[Modifier] = Seq()

    override def edit() = input(field,true,modifiers){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextArea.apply(stringModel,None,y:_*)
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,field.label,modifiers))
  }

  class TwoLines(field:JSONField, prop: Property[Json]) extends Textarea(field,prop) {

    override val modifiers: Seq[JsDom.all.Modifier] = Seq(rows := 2)
  }



  case class Number(field:JSONField, prop: Property[Json]) extends Widget {



    override def edit():JsDom.all.Modifier = (input(field,true){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToNumericJson _)
      NumberInput.apply(stringModel,None,y:_*)
    })
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,field.label))
  }

  case class NumberNoLabel(field:JSONField, prop: Property[Json]) extends Widget {



    override def edit():JsDom.all.Modifier = (input(field,false){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToNumericJson _)
      NumberInput.apply(stringModel,None,y:_*)
    })
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,None,Seq()))
  }




}

