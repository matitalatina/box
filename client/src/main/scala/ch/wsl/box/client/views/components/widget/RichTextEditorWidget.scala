package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.utils.{BrowserConsole, ClientConf}
import ch.wsl.box.client.vendors.Quill
import ch.wsl.box.model.shared.JSONField
import ch.wsl.box.shared.utils.JSONUtils._
import io.circe.Json
import io.circe.syntax._
import io.udash._
import io.udash.properties.single.Property
import scalatags.JsDom
import scribe.Logging

import scala.util.Try

case class RichTextEditorWidget(_id: Property[String], field: JSONField, prop: Property[Json]) extends Widget with Logging {
  import scalacss.ScalatagsCss._
  import scalatags.JsDom.all._

  override protected def show(): JsDom.all.Modifier = autoRelease(produce(prop){ p =>
    div(p.string).render
  })

  _id.listen(x => logger.info(s"Ritch text widget load with ID: $x"))

  override protected def edit(): JsDom.all.Modifier = {
    produce(_id) { _ =>
      println("reload RTEW")
      val container = div( height := 300.px).render
      val parent = div(container).render
      BrowserConsole.log(parent)
      Quill.load(container, prop.get.string, field.placeholder.getOrElse(""),{ s:String => prop.set(s.asJson)})
      div(
        parent
      ).render

    }
  }

}


object RichTextEditorWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = RichTextEditorWidget(id,field,prop)
}