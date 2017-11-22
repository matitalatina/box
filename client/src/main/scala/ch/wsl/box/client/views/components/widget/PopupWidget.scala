package ch.wsl.box.client.views.components.widget
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.model.shared.JSONFieldOptions
import io.circe._
import io.circe.syntax._
import io.udash._
import io.udash.properties.single.Property
import org.scalajs.dom.Event
import ch.wsl.box.shared.utils.JsonUtils._

import scalacss.ScalatagsCss._
import scalatags.JsDom.all.{label => lab}
import scalatags.JsDom.all._

case class PopupWidget(options:JSONFieldOptions,label: String, prop: Property[Json]) extends OptionWidget {
  override def render() = {


    val selectModel: Property[String] = prop.transform(value2Label,label2Value)

    div(BootstrapCol.md(12),GlobalStyles.noPadding)(
      if(label.length >0) lab(label) else {},
      span(bind(selectModel),button("Change", onclick :+= ((e:Event) => println(e))))
    )
  }
}
