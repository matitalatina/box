package ch.wsl.box.client.views.components.widget
import io.circe.Json
import io.udash.properties.single.Property
import io.udash._
import scalatags.JsDom
import scalatags.JsDom.all._

object HiddenWidget extends Widget {
  override protected def show(): JsDom.all.Modifier = {}

  override protected def edit(): JsDom.all.Modifier = {}
}
