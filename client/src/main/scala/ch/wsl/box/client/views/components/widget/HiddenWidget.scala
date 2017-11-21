package ch.wsl.box.client.views.components.widget
import io.circe.Json
import io.udash.properties.single.Property
import io.udash._
import scalatags.JsDom.all._

object HiddenWidget extends Widget {
  override def render(key: Property[String], label: String, prop: Property[Json]) = {}
}
