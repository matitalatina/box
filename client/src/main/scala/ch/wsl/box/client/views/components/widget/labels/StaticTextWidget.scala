package ch.wsl.box.client.views.components.widget.labels

import ch.wsl.box.client.views.components.widget.{ComponentWidgetFactory, Widget}
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import scalatags.JsDom
import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import io.udash.css.CssView._


object StaticTextWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = StaticTextWidgetImpl(field)

  case class StaticTextWidgetImpl(field:JSONField) extends Widget {

    override protected def show(): JsDom.all.Modifier = p(field.label.getOrElse(field.name))


    override protected def edit(): JsDom.all.Modifier = show()
  }
}