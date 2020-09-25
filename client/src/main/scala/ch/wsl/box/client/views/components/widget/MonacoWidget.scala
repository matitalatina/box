package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.utils.{BrowserConsole, ClientConf}
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash.properties.single.Property
import scalatags.JsDom
import scribe.Logging
import io.udash._
import ch.wsl.box.shared.utils.JSONUtils._
import io.circe.syntax._
import org.scalajs.dom.html.Div
import typings.monacoEditor.mod.editor.{IStandaloneCodeEditor, IStandaloneEditorConstructionOptions}

import scala.util.Try

case class MonacoWidget(_id: Property[Option[String]], field: JSONField, prop: Property[Json]) extends Widget with Logging {
  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._

  override protected def show(): JsDom.all.Modifier = autoRelease(produce(prop){ p =>
    div(p.string).render
  })

  var container: Div = null


  override def afterRender(): Unit = {
    if(container != null) {
      val language = Try(field.widget.get.split("\\.").last).getOrElse("html")
      val editor = typings.monacoEditor.mod.editor.create(container,IStandaloneEditorConstructionOptions()
        .setLanguage(language)
        .setValue(prop.get.string)

      )
      editor.onDidChangeModelContent(e => prop.set(editor.getValue().asJson))
    }
  }

  override protected def edit(): JsDom.all.Modifier = {

    autoRelease(produce(_id) { _ =>
      container = div(ClientConf.style.editor).render

      //Monaco.load(container,language,prop.get.string,{s:String => prop.set(s.asJson)})
      div(
        container
      ).render

    })
  }

}

object MonacoWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField) = MonacoWidget(id,field,prop)
}
