package ch.wsl.box.client.views.components.widget
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.model.shared.{JSONField, JSONFieldLookup, JSONFieldTypes, JSONLookup, JSONMetadata, WidgetsNames}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import ch.wsl.box.client.Context._
import ch.wsl.box.client.services.ClientConf
import io.circe
import scalatags.JsDom.all._
import scalatags.JsDom.all.{label => lab}
import scalacss.ScalatagsCss._
import io.udash.css.CssView._
import scalatags.JsDom
import scribe.Logging

import scala.concurrent.Future

case class SelectWidgetFactory(allData:Property[Json]) extends ComponentWidgetFactory  {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = new SelectWidget(field, prop, allData)
}

case class SelectWidgetFullWidthFactory(allData:Property[Json]) extends ComponentWidgetFactory  {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = new SelectWidgetFullWidth(field, prop, allData)
}

class SelectWidget(val field:JSONField, data: Property[Json], val allData:Property[Json]) extends  LookupWidget with Logging {


  val modifiers:Seq[Modifier] = Seq()


  val selectModel = data.bitransform(value2Label)(label2Value)

  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JSONUtils._

  override def beforeSave(data: Json, metadata: JSONMetadata) = Future.successful{
    val jsField = data.js(field.name)
    val result = if (!field.nullable && jsField.isNull) {
      lookup.get.headOption.map(_.id) match {
        case Some(v) => v.asJson
        case None => Json.Null
      }
    } else jsField
    Map(field.name -> result).asJson

  }


  override protected def show(): JsDom.all.Modifier = autoRelease(WidgetUtils.showNotNull(data){ _ =>
    div(BootstrapCol.md(12),ClientConf.style.noPadding)(
      lab(field.title),
      div(BootstrapStyles.Float.right(), bind(selectModel)),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  })

  override def edit() = {


    val model:Property[JSONLookup] = field.`type` match {
      case "number" =>  data.bitransform[JSONLookup](
        {json:Json =>
          val id = jsonToString(json)
          lookup.get.find(_.id == jsonToString(json)).getOrElse(JSONLookup(id,id + " NOT FOUND"))
        })(
        {jsonLookup:JSONLookup => strToNumericJson(jsonLookup.id)}
      )
      case _ => data.bitransform[JSONLookup](
        {json:Json =>
          val id = jsonToString(json)
          lookup.get.find(_.id == id).getOrElse(JSONLookup(id,id + " NOT FOUND"))
        })(
        {jsonLookup:JSONLookup => strToJson(field.nullable)(jsonLookup.id)}
      )
    }


    lookup.listen({lookups =>
      if(!lookups.contains(model.get)) {
        model.set(lookups.headOption.getOrElse(JSONLookup("","")))
      }
    })


    val m:Seq[Modifier] = Seq[Modifier](BootstrapStyles.Float.right())++modifiers++WidgetUtils.toNullable(field.nullable)

    val tooltip = WidgetUtils.addTooltip(field.tooltip) _

    div(BootstrapCol.md(12),ClientConf.style.noPadding, ClientConf.style.smallBottomMargin)(
      WidgetUtils.toLabel(field),
      tooltip(Select[JSONLookup](model,lookup)((s:JSONLookup) => StringFrag(s.value),m:_*).render),
      div(BootstrapStyles.Visibility.clearfix)
    )
  }
}

class SelectWidgetFullWidth(field:JSONField, prop: Property[Json],allData:Property[Json]) extends SelectWidget(field,prop,allData) {

  override val modifiers: Seq[JsDom.all.Modifier] = Seq(width := 100.pct)
}
