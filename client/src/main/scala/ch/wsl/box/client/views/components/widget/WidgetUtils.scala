package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.Labels
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash.bindings.modifiers.Binding
import io.udash.bootstrap.tooltip.UdashTooltip
import io.udash.produce
import io.udash.properties.single.Property
import org.scalajs.dom
import org.scalajs.dom.Element
import scalatags.JsDom.all.{Modifier, label}

import scala.concurrent.duration.DurationInt

object WidgetUtils {

  import scalacss.ScalatagsCss._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._

  def showNotNull(prop:Property[Json])(f: Json => Seq[Element]):Binding = produce(prop) {
    case Json.Null => Seq()
    case p:Json =>  f(p)
  }

  def addTooltip(tooltip:Option[String],placement:UdashTooltip.Placement = UdashTooltip.BottomPlacement)(el:dom.Node) = {
    tooltip match {
      case Some(tip) => UdashTooltip(
        trigger = Seq(UdashTooltip.HoverTrigger),
        delay = UdashTooltip.Delay(500 millis, 250 millis),
        placement = (_, _) => Seq(placement),
        title = (_) => tip
      )(el)
      case _ => {}
    }
    el
  }

  def toLabel(field:JSONField) = {

    val labelStyle = field.nullable match {
      case true => GlobalStyles.labelNonRequred
      case false => GlobalStyles.labelRequired
    }


    val boxLabel = label(
      labelStyle,
      field.title,if(!field.nullable) small(GlobalStyles.smallLabelRequired ," - " + Labels.form.required) else {}
    ).render

    //addTooltip(field.tooltip,boxLabel)

    boxLabel
  }

  def toNullable(nullable: Boolean):Seq[Modifier]={
    nullable match{
      case true => Seq.empty
      case false => Seq(required := true,GlobalStyles.notNullable)
    }
  }

}
