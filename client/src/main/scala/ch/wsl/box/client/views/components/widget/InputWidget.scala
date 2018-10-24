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
import org.scalajs.dom.Node

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


  //used in read-only mode
  private def showInput(prop:Property[Json],field:JSONField, withLabel:Boolean, modifiers:Seq[Modifier] = Seq()):Binding = WidgetUtils.showNotNull(prop){ p =>

    val inputRendererDefaultModifiers:Seq[Modifier] = Seq(BootstrapStyles.pullRight)

    def reallyWithLabel = withLabel & (field.title.length > 0)


    div(BootstrapCol.md(12),GlobalStyles.noPadding,GlobalStyles.smallBottomMargin,
      if(reallyWithLabel) label(field.title) else {},
      if(reallyWithLabel)
        div(inputRendererDefaultModifiers++modifiers, (bind(prop.transform(_.string))))
      else
        div(inputRendererDefaultModifiers++modifiers++Seq(width := 100.pct), (bind(prop.transform(_.string)))),
      div(BootstrapStyles.Visibility.clearfix)
    ).render

  }

  private def input(field:JSONField, withLabel:Boolean, skipRequiredInfo:Boolean=false, modifiers:Seq[Modifier] = Seq())(inputRenderer:(Seq[Modifier]) => Node):Modifier = {

    val inputRendererDefaultModifiers:Seq[Modifier] = Seq(BootstrapStyles.pullRight)

    def reallyWithLabel = withLabel & (field.title.length > 0)

    val ph = field.placeholder match{
      case Some(p) if p.nonEmpty => Seq(placeholder := p)
      case _ => Seq.empty
    }

    val tooltip = WidgetUtils.addTooltip(field.tooltip) _


    val allModifiers = inputRendererDefaultModifiers++ph++ WidgetUtils.toNullable(field.nullable) ++modifiers

    div(BootstrapCol.md(12),GlobalStyles.noPadding,GlobalStyles.smallBottomMargin,
      if(reallyWithLabel) WidgetUtils.toLabel(field, skipRequiredInfo) else {},
      if(reallyWithLabel)
        tooltip(inputRenderer(allModifiers))
      else
        tooltip(inputRenderer(allModifiers++Seq(width := 100.pct))),
      div(BootstrapStyles.Visibility.clearfix)
    )

  }

  class Text(val field:JSONField, prop: Property[Json]) extends Widget {

    val modifiers:Seq[Modifier] = Seq()


    override def edit() = input(field,true, false, modifiers){ case y =>

      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextInput(stringModel)(y:_*).render
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,field,true, modifiers))
  }

  class TextDisabled(field:JSONField, prop: Property[Json]) extends Text(field,prop) {

    override def edit() = Conf.manualEditKeyFields match{
//      case false =>{    //todo : mimic an input with a label (otherwise it is not safe: can change dom and save new key!)
//        show()
//      }
//      case true => {
        case _ => {
        input(field,true, !Conf.manualEditKeyFields, modifiers){ case y =>
          val stringModel = prop.transform[String](jsonToString _,strToJson _)
          TextInput(stringModel)(y:_*).render
        }
      }

    }

    override val modifiers = Seq({if (!Conf.manualEditKeyFields) {disabled := true} else {}} , textAlign.right)
  }

  case class TextNoLabel(field:JSONField, prop: Property[Json]) extends Widget {


    override def edit() = input(field,false, false){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextInput(stringModel)(y:_*).render
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,field, false))
  }

  class Textarea(val field:JSONField, prop: Property[Json]) extends Widget {

    val modifiers:Seq[Modifier] = Seq()

    override def edit() = input(field,true, false, modifiers){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextArea(stringModel)(y:_*).render
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,field,true,modifiers))
  }

  class TwoLines(field:JSONField, prop: Property[Json]) extends Textarea(field,prop) {

    override val modifiers: Seq[JsDom.all.Modifier] = Seq(rows := 2)
  }



  case class Number(field:JSONField, prop: Property[Json]) extends Widget {



    override def edit():JsDom.all.Modifier = (input(field, true, false){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToNumericJson _)
      NumberInput(stringModel)(y:_*).render
    })
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop, field, true))
  }

  case class NumberNoLabel(field:JSONField, prop: Property[Json]) extends Widget {



    override def edit():JsDom.all.Modifier = (input(field,false, false){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToNumericJson _)
      NumberInput(stringModel)(y:_*).render
    })
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop, field, false,Seq()))
  }




}

