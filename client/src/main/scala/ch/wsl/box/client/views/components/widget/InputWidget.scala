package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property

import scalatags.JsDom.all._

class InputWidget(hasLabel:Boolean,modifiers:Modifier*) {


  import scalacss.ScalatagsCss._

  private def input(labelString:Option[String] = None)(inputRenderer:(Seq[Modifier]) => Modifier):Modifier = {

    val inputRendererDefaultModifiers:Seq[Modifier] = Seq(BootstrapStyles.pullRight,textAlign.right)

    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      if(hasLabel && labelString.exists(_.length > 0)) label(labelString) else {},
      inputRenderer(inputRendererDefaultModifiers++modifiers),
      div(BootstrapStyles.Visibility.clearfix)
    )

  }

  case class Text(label: String, prop: Property[Json]) extends Widget {
    override def render() = input(Some(label)){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextInput.apply(stringModel,None,y:_*)
    }
  }

  case class Textarea(label: String, prop: Property[Json]) extends Widget {
    override def render() = input(Some(label)){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToJson _)
      TextArea.apply(stringModel,None,y:_*)
    }
  }

  case class Number(label: String, prop: Property[Json]) extends Widget {
    override def render() = input(Some(label)){ case y =>
      val stringModel = prop.transform[String](jsonToString _,strToNumericJson _)
      NumberInput.apply(stringModel,None,y:_*)
    }
  }
}

object InputWidget {
  def noLabel(modifiers:Modifier*) = new InputWidget(false,modifiers);
  def apply(modifiers:Modifier*): InputWidget = new InputWidget(true, modifiers)
}
