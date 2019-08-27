package ch.wsl.box.client.views.components.widget
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.model.shared.{JSONField, JSONFieldLookup, JSONFieldTypes, JSONMetadata, WidgetsNames}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import ch.wsl.box.client.Context._
import ch.wsl.box.client.utils.ClientConf
import io.circe
import scalatags.JsDom.all._
import scalatags.JsDom.all.{label => lab}
import scalacss.ScalatagsCss._
import io.udash.css.CssView._
import scalatags.JsDom
import scribe.Logging

import scala.concurrent.Future

object SelectWidget extends ComponentWidgetFactory  {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = new SelectWidget(field, prop)
}

object SelectWidgetFullWidth extends ComponentWidgetFactory  {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = new SelectWidgetFullWidth(field, prop)
}

class SelectWidget(val field:JSONField, prop: Property[Json]) extends  LookupWidget with Logging {

  val modifiers:Seq[Modifier] = Seq()


  val selectModel = prop.transform(value2Label,label2Value)

  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JSONUtils._

  override def beforeSave(data: Json, metadata: JSONMetadata) = Future.successful{
    val jsField = data.js(field.name)
    val result = if (!field.nullable && jsField.isNull) {
      lookup.lookup.headOption.map(_.id) match {
        case Some(v) => v.asJson
        case None => Json.Null
      }
    } else jsField

    val js = data.asObject match {
      case Some(obj) => obj.add(field.name,result).asJson
      case None => Map(field.name -> result).asJson
    }
    js
  }


  override protected def show(): JsDom.all.Modifier = autoRelease(WidgetUtils.showNotNull(prop){ _ =>
    div(BootstrapCol.md(12),ClientConf.style.noPadding)(
      lab(field.title),
      div(BootstrapStyles.pullRight, bind(selectModel)),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  })

  override def edit() = {


    val opts = if(field.nullable) {
      Seq("") ++ lookup.lookup.map(_.value)
    } else {
      lookup.lookup.map(_.value)
    }

    val m:Seq[Modifier] = Seq[Modifier](BootstrapStyles.pullRight)++modifiers++WidgetUtils.toNullable(field.nullable)

    val tooltip = WidgetUtils.addTooltip(field.tooltip) _

    div(BootstrapCol.md(12),ClientConf.style.noPadding)(
      WidgetUtils.toLabel(field),
      tooltip(Select(selectModel,opts,Select.defaultLabel)(m).render),
      div(BootstrapStyles.Visibility.clearfix)
    )
  }
}

class SelectWidgetFullWidth(field:JSONField, prop: Property[Json]) extends SelectWidget(field,prop) {

  override val modifiers: Seq[JsDom.all.Modifier] = Seq(width := 100.pct)
}
