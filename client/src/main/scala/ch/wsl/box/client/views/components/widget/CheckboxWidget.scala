package ch.wsl.box.client.views.components.widget
import io.circe._
import io.circe.syntax._
import io.udash._
import io.udash.properties.single.Property
import ch.wsl.box.client.Context._
import scalatags.JsDom
import scalatags.JsDom.all._
import io.udash.css.CssView._
import ch.wsl.box.shared.utils.JsonUtils._

case class CheckboxWidget(label: String, prop: Property[Json]) extends Widget {
  override def edit() = {
    def jsToBool(json:Json):Boolean = json.asNumber.flatMap(_.toInt).exists(_ == 1)
    def boolToJson(v:Boolean):Json = v match {
      case true => 1.asJson
      case false => 0.asJson
    }
    val booleanModel = prop.transform[Boolean](jsToBool _ ,boolToJson _)
    div(
      autoRelease(Checkbox(booleanModel)()), " ", label
    )
  }

  override protected def show(): JsDom.all.Modifier = WidgetUtils.showNotNull(prop) { p =>
    div(
        if(
          p.as[Boolean].right.toOption.contains(true) ||
          p.as[Int].right.toOption.contains(1)
        ) raw("&#10003;") else raw("&#10005;"), " ", label
      ).render
  }
}
