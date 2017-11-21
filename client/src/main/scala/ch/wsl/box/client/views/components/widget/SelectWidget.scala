package ch.wsl.box.client.views.components.widget
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.model.shared.{JSONField, JSONFieldOptions}
import io.circe.Json
import io.udash.Select
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import ch.wsl.box.client.Context._
import scalatags.JsDom.all._
import scalatags.JsDom.all.{label => lab}
import scalacss.ScalatagsCss._

case class SelectWidget(options:JSONFieldOptions,field:JSONField)extends OptionWidget {

  override def render(key: Property[String], label: String, prop: Property[Json]) = {

    val selectModel = prop.transform(value2Label,label2Value)

    val opts = if(field.nullable) {
      Seq("") ++ options.options.values.toSeq
    } else {
      options.options.values.toSeq
    }

    div(BootstrapCol.md(12),GlobalStyles.noPadding)(
      if(label.length >0) lab(label) else {},
      Select(selectModel,opts,Select.defaultLabel)(BootstrapStyles.pullRight),
      div(BootstrapStyles.Visibility.clearfix)
    )
  }
}
