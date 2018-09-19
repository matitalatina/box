package ch.wsl.box.client.views.components.widget

import io.circe.Json
import io.udash.produce
import io.udash.properties.single.Property
import org.scalajs.dom.Element
import scalatags.JsDom.all.Modifier

object WidgetUtils {
  def showNotNull(prop:Property[Json])(f: Json => Seq[Element]):Modifier = produce(prop) {
    case Json.Null => Seq()
    case p:Json =>  f(p)
  }
}
