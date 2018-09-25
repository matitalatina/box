package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import scalatags.JsDom

import scala.concurrent.Future
import scalatags.JsDom.all._
import ch.wsl.box.shared.utils.JsonUtils._
import io.udash.bindings.modifiers.Binding

class InputWidget(hasLabel:Boolean,modifiers:Modifier*) {


  import scalacss.ScalatagsCss._
  import io.udash.css.CssView._

  private def showInput(prop:Property[Json],labelString:String):Binding = WidgetUtils.showNotNull(prop){ p =>

    val inputRendererDefaultModifiers:Seq[Modifier] = Seq(BootstrapStyles.pullRight)

    def withLabel = hasLabel && labelString.length > 0

    div(BootstrapCol.md(12),GlobalStyles.noPadding,GlobalStyles.smallBottomMargin,
      if(withLabel) label(labelString) else {},
      if(withLabel)
        div(inputRendererDefaultModifiers++modifiers, bind(prop.transform(_.string)))
      else
        div(inputRendererDefaultModifiers++modifiers++Seq(width := 100.pct), bind(prop.transform(_.string))),
      div(BootstrapStyles.Visibility.clearfix)
    ).render

  }

  private def input(labelString:Option[String] = None)(inputRenderer:(Seq[Modifier]) => Modifier):Modifier = {

    val inputRendererDefaultModifiers:Seq[Modifier] = Seq(BootstrapStyles.pullRight)

    def withLabel = hasLabel && labelString.exists(_.length > 0)

    div(BootstrapCol.md(12),GlobalStyles.noPadding,GlobalStyles.smallBottomMargin,
      if(withLabel) label(labelString) else {},
      if(withLabel)
        inputRenderer(inputRendererDefaultModifiers++modifiers)
      else
        inputRenderer(inputRendererDefaultModifiers++modifiers++Seq(width := 100.pct)),
      div(BootstrapStyles.Visibility.clearfix)
    )

  }

  case class Text(label: String, prop: Property[Json]) extends Widget {

    override def edit() = input(Some(label)){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextInput.apply(stringModel,None,y:_*)
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,label))
  }

  case class Textarea(label: String, prop: Property[Json]) extends Widget {
    override def edit() = input(Some(label)){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextArea.apply(stringModel,None,y:_*)
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,label))
  }

  case class Number(label: String, prop: Property[Json]) extends Widget {
    override def edit() = input(Some(label)){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToNumericJson _)
      NumberInput.apply(stringModel,None,y:_*)
    }
    override protected def show(): JsDom.all.Modifier = autoRelease(showInput(prop,label))
  }
}

object InputWidget {
  def noLabel(modifiers:Modifier*) = new InputWidget(false,modifiers);
  def apply(modifiers:Modifier*): InputWidget = new InputWidget(true, modifiers)
}
