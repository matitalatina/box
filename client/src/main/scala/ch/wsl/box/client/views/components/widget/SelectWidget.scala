package ch.wsl.box.client.views.components.widget
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.model.shared.{JSONField, JSONFieldLookup}
import io.circe.Json
import io.udash.Select
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import ch.wsl.box.client.Context._
import scalatags.JsDom.all._
import scalatags.JsDom.all.{label => lab}
import scalacss.ScalatagsCss._

case class SelectWidget(lookup:JSONFieldLookup, field:JSONField, label: String, prop: Property[Json], modifiers: Modifier*) extends LookupWidget {

  override def render() = {

    val selectModel = prop.transform(value2Label,label2Value)

    val opts = if(field.nullable) {
      Seq("") ++ lookup.lookup.map(_.value)
    } else {
      lookup.lookup.map(_.value)
    }

    val m:Seq[Modifier] = Seq[Modifier](BootstrapStyles.pullRight)++modifiers

    div(BootstrapCol.md(12),GlobalStyles.noPadding)(
      if(label.length >0) lab(label) else {},
      Select(selectModel,opts,Select.defaultLabel)(m),
      div(BootstrapStyles.Visibility.clearfix)
    )
  }
}
