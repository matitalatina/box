package ch.wsl.box.client.views.components.widget.labels

import ch.wsl.box.client.views.components.widget.{ComponentWidgetFactory, Widget}
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import scalatags.JsDom
import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import io.udash.css.CssView._


case class TitleWidget(level:Int) extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = H1WidgetImpl(field)

  case class H1WidgetImpl(field:JSONField) extends Widget {

    override protected def show(): JsDom.all.Modifier = level match {
      case 1 => h1(field.label.getOrElse(field.name))
      case 2 => h2(field.label.getOrElse(field.name))
      case 3 => h3(field.label.getOrElse(field.name))
      case 4 => h4(field.label.getOrElse(field.name))
      case 5 => h5(field.label.getOrElse(field.name))
    }

    override protected def edit(): JsDom.all.Modifier = show()
  }
}


