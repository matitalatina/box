package ch.wsl.box.client.views.components.widget
import io.circe._
import io.circe.syntax._
import io.udash.Checkbox
import io.udash.properties.single.Property
import ch.wsl.box.client.Context._

import scalatags.JsDom.all._

object CheckboxWidget extends Widget {
  override def render(key: Property[String], label: String, prop: Property[Json]) = {
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
}
