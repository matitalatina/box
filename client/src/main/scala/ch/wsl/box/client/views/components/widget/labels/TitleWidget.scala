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

    val text:String = field.label.getOrElse(field.name)

    override protected def show(): JsDom.all.Modifier = level match {
      case 1 => h1(text)
      case 2 => h2(text)
      case 3 => h3(text)
      case 4 => h4(text)
      case 5 => h5(text)
    }

    override protected def edit(): JsDom.all.Modifier = show()
  }
}


