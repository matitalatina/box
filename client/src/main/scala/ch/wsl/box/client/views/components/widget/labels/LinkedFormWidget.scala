package ch.wsl.box.client.views.components.widget.labels

import ch.wsl.box.client.RoutingState
import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.Navigate
import ch.wsl.box.client.views.components.widget.lookup.DynamicLookupWidget
import ch.wsl.box.client.views.components.widget.{ComponentWidgetFactory, Widget, WidgetParams}
import ch.wsl.box.model.shared._
import ch.wsl.box.shared.utils.JSONUtils.EnhancedJson
import io.circe.Json
import io.udash._
import org.scalajs.dom.Event
import scalatags.JsDom
import scalatags.JsDom.all._
import scribe.Logging

object LinkedFormWidget extends ComponentWidgetFactory {


  override def name: String = WidgetsNames.linkedForm

  override def create(params: WidgetParams): Widget = LinkedFormWidgetImpl(params)

  case class LinkedFormWidgetImpl(params: WidgetParams) extends Widget with Logging {

    val field: JSONField = params.field

    def navigate(goTo: Routes => RoutingState) = (e: Event) => field.linked.map(l => Navigate.to(goTo(Routes(EntityKind.FORM.kind, l.name))))

    val label = field.linked.flatMap(_.label).orElse(field.linked.map(_.name)).getOrElse("Open")

    override protected def show(): Modifier = a(label, onclick :+= navigate(_.entity()))

    override protected def edit(): Modifier = show()
  }

}
