package ch.wsl.box.client.views.components.widget.labels

import ch.wsl.box.client.RoutingState
import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.Navigate
import ch.wsl.box.client.views.components.widget.{ComponentWidgetFactory, Widget, WidgetParams}
import ch.wsl.box.model.shared.{Child, EntityKind, JSONField, JSONFieldLookup, JSONID, JSONKeyValue, JSONMetadata, LinkedForm, WidgetsNames}
import io.circe.Json
import io.udash._
import scalatags.JsDom.all._
import ch.wsl.box.shared.utils.JSONUtils._
import org.scalajs.dom.Event
import scribe.Logging

object LinkedFormWidget extends ComponentWidgetFactory {


  override def name: String = WidgetsNames.linkedForm

  override def create(params: WidgetParams): Widget = LinkedFormWidgetImpl(params.field,params.allData)

  case class LinkedFormWidgetImpl(field:JSONField,parentData:Property[Json]) extends Widget with Logging {

    val linked:LinkedForm = field.linked.get

    val linkedData = parentData.transform{js =>

      def findLookup(k:String):String = {
        k match {
          case "lookup" if field.lookup.isDefined => {
            val lookupKey = js.get(field.lookup.get.map.localValueProperty)
            logger.debug(
              s"""
                 |field: ${field.lookup.get.map.localValueProperty}
                 |key: $lookupKey
                 |js: $js
                 |""".stripMargin)
            field.lookup.get.lookup.find(_.id == lookupKey).map(_.value).getOrElse(lookupKey)
          }
          case _ => js.get(k)
        }
      }

      val parentValues = linked.parentValueFields.map(k => findLookup(k))
      (
        JSONID.fromMap(linked.childValueFields.zip(parentValues).toMap),
        linked.parentLabelFields.map(k => findLookup(k)).mkString(" - ")
      )
    }

    def navigate(goTo:Routes => RoutingState ) = (e:Event) => Navigate.to(goTo(Routes(EntityKind.FORM.kind,linked.name)))

    override protected def show(): Modifier = produce(linkedData){ case (id,lab) => a(lab, onclick :+= navigate(_.show(id.asString))).render }
    override protected def edit(): Modifier = produce(linkedData){ case (id,lab) => a(lab, onclick :+= navigate(_.edit(id.asString))).render }
  }

}
