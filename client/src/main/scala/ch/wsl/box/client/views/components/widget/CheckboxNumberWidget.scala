package ch.wsl.box.client.views.components.widget

import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes, WidgetsNames}
import io.circe._
import io.circe.syntax._
import io.udash._
import io.udash.bootstrap.tooltip.UdashTooltip
import scalatags.JsDom
import scalatags.JsDom.all._

case class CheckboxNumberWidget(field:JSONField, prop: Property[Json]) extends Widget {

  val name = WidgetsNames.checkboxNumber
  val supportedTypes = Seq(JSONFieldTypes.NUMBER)

  override def edit() = {
    def jsToBool(json:Json):Boolean = json.asNumber.flatMap(_.toInt).exists(_ == 1)
    def boolToJson(v:Boolean):Json = v match {
      case true => 1.asJson
      case false => 0.asJson
    }

    val tooltip = WidgetUtils.addTooltip(field.tooltip,UdashTooltip.Placement.Right) _

    val booleanModel = prop.transform[Boolean](jsToBool _ ,boolToJson _)
    div(
      tooltip(Checkbox(booleanModel)().render), " ", WidgetUtils.toLabel(field)
    )
  }

  override protected def show(): JsDom.all.Modifier = WidgetUtils.showNotNull(prop) { p =>
    div(
        if(
          p.as[Boolean].right.toOption.contains(true) ||
          p.as[Int].right.toOption.contains(1)
        ) raw("&#10003;") else raw("&#10005;"), " ", field.title
      ).render
  }
}


object CheckboxNumberWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = CheckboxNumberWidget(field,prop)
}
