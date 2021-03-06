package ch.wsl.box.client.views.components.widget
import io.circe._
import io.circe.syntax._
import io.udash._
import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes, WidgetsNames}
import scalatags.JsDom
import scalatags.JsDom.all._
import io.udash.css.CssView._
import ch.wsl.box.shared.utils.JSONUtils._
import io.udash.bootstrap.tooltip.UdashTooltip

case class CheckboxWidget(field:JSONField, data: Property[Json]) extends Widget with HasData {

  def jsToBool(json:Json):Boolean = field.`type` match {
    case JSONFieldTypes.BOOLEAN => json.asBoolean.getOrElse(false)
    case JSONFieldTypes.NUMBER => json.asNumber.flatMap(_.toInt).exists(_ == 1)
    case _ => false
  }

  def boolToJson(v:Boolean):Json = field.`type` match {
    case JSONFieldTypes.BOOLEAN => v.asJson
    case JSONFieldTypes.NUMBER => v match {
        case true => 1.asJson
        case false => 0.asJson
    }
    case _ => Json.Null
  }

  override def edit() = {

    val tooltip = WidgetUtils.addTooltip(field.tooltip,UdashTooltip.Placement.Right) _

    val booleanModel = Property(false)

    autoRelease(data.sync[Boolean](booleanModel)(js => jsToBool(js),bool => boolToJson(bool)))

    div(
      tooltip(Checkbox(booleanModel)().render), " ", WidgetUtils.toLabel(field)
    )
  }

  override protected def show(): JsDom.all.Modifier = WidgetUtils.showNotNull(data) { p =>
    div(
        if(
          p.as[Boolean].right.toOption.contains(true) ||
          p.as[Int].right.toOption.contains(1)
        ) raw("&#10003;") else raw("&#10005;"), " ", field.title
      ).render
  }
}

object CheckboxWidget extends ComponentWidgetFactory {


  override def name: String = WidgetsNames.checkbox


  override def create(params: WidgetParams): Widget = CheckboxWidget(params.field,params.prop)

}
