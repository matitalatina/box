package ch.wsl.box.client.views.components.widget
import io.circe._
import io.circe.syntax._
import io.udash._
import io.udash.properties.single.Property
import ch.wsl.box.client.Context._
import scalatags.JsDom
import scalatags.JsDom.all._

case class CheckboxWidget(label: String, prop: Property[Json]) extends Widget {
  override def edit() = {
    def jsToBool(json:Json):Boolean = json.asNumber.flatMap(_.toInt).exists(_ == 1)
    def boolToJson(v:Boolean):Json = v match {
      case true => 1.asJson
      case false => 0.asJson
    }
    val booleanModel = prop.transform[Boolean](jsToBool _ ,boolToJson _)
    div(
      Checkbox(booleanModel), " ", label
    )
  }

  override protected def show(): JsDom.all.Modifier = div(
    bind(prop), " ", label
  )
}
