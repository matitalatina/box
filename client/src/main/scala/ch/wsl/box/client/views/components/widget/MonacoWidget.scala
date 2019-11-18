package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.utils.ClientConf
import ch.wsl.box.client.vendors.Monaco
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash.properties.single.Property
import scalatags.JsDom
import scribe.Logging
import io.udash._
import ch.wsl.box.shared.utils.JSONUtils._
import io.circe.syntax._

import scala.util.Try

case class MonacoWidget(_id: Property[String], field: JSONField, prop: Property[Json]) extends Widget with Logging {
  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._

  override protected def show(): JsDom.all.Modifier = autoRelease(produce(prop){ p =>
    div(p.string).render
  })

  override protected def edit(): JsDom.all.Modifier = {
    println(field.widget)
    val language = Try(field.widget.get.split("\\.").last).getOrElse("html")
    autoRelease(produce(_id) { _ =>
      val container = div(ClientConf.style.editor).render
      Monaco.load(container,language,prop.get.string,{s:String => prop.set(s.asJson)})
      div(
        container
      ).render

    })
  }

}

object MonacoWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = MonacoWidget(id,field,prop)
}
