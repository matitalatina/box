package ch.wsl.box.client.views.components.widget
import io.circe._
import io.circe.syntax._
import io.udash._
import ch.wsl.box.client.Context._
import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes, WidgetsNames}
import scalatags.JsDom
import scalatags.JsDom.all._
import io.udash.css.CssView._
import ch.wsl.box.shared.utils.JSONUtils._
import io.udash.bootstrap.tooltip.UdashTooltip

case class CheckboxWidget(field:JSONField, prop: Property[Json]) extends Widget {

  val name = WidgetsNames.checkbox
  val supportedTypes = Seq(JSONFieldTypes.BOOLEAN)

  override def edit() = {
    def jsToBool(json:Json):Boolean = json.asBoolean.getOrElse(false)
    def boolToJson(v:Boolean):Json = v.asJson

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

object CheckboxWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = CheckboxWidget(field,prop)
}
