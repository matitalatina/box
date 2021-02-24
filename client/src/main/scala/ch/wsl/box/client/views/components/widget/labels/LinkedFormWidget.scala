package ch.wsl.box.client.views.components.widget.labels

import ch.wsl.box.client.RoutingState
import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.{ClientConf, Navigate}
import ch.wsl.box.client.views.components.widget.lookup.DynamicLookupWidget
import ch.wsl.box.client.views.components.widget.{ComponentWidgetFactory, Widget, WidgetParams}
import ch.wsl.box.model.shared._
import ch.wsl.box.shared.utils.JSONUtils.EnhancedJson
import io.circe._
import io.circe.generic.auto._
import io.udash._
import org.scalajs.dom.Event
import scalacss.internal.Color
import scalatags.JsDom
import scalatags.JsDom.all._
import scribe.Logging

object LinkedFormWidget extends ComponentWidgetFactory {

  case class Param(style:String,color:Option[String],background:Option[String])

  override def name: String = WidgetsNames.linkedForm

  override def create(params: WidgetParams): Widget = LinkedFormWidgetImpl(params)

  case class LinkedFormWidgetImpl(params: WidgetParams) extends Widget with Logging {

    import io.udash.css.CssView._
    import scalacss.ScalatagsCss._


    val field: JSONField = params.field

    def navigate(goTo: Routes => RoutingState) = (e: Event) => field.linked.map(l => Navigate.to(goTo(Routes(EntityKind.FORM.kind, l.name))))

    val label = field.linked.flatMap(_.label).orElse(field.linked.map(_.name)).getOrElse("Open")

    val linkParam = params.field.params.flatMap(_.as[Param] match {
      case Left(value) => {
        logger.warn(value.message)
        None
      }
      case Right(value) =>Some(value)
    })

    logger.info(s"Param: $linkParam, json ${params.field.params}")

    override protected def show(): Modifier = linkParam match {
      case Some(Param(style,_color,background)) if style == "box" => {
        a(onclick :+= navigate(_.entity()),
          div(ClientConf.style.boxedLink,
            color := _color.getOrElse(ClientConf.colorLink),
            backgroundColor := background.getOrElse(ClientConf.colorMain),
            label
          )
        )
      }
      case _ =>  a(label, onclick :+= navigate(_.entity()))
    }

    override protected def edit(): Modifier = show()
  }

}
