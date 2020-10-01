package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.utils.{BrowserConsole, ClientConf}
import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes}
import io.circe.Json
import io.udash.properties.single.Property
import scalatags.JsDom
import scribe.Logging
import io.udash._
import ch.wsl.box.shared.utils.JSONUtils._
import io.circe.syntax._
import io.circe.parser._
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

  val defaultLanguage = field.`type` match {
    case JSONFieldTypes.JSON => "json"
    case _ => "html"
  }

  val language = field.params.flatMap(_.getOpt("language")).getOrElse(defaultLanguage)
  val containerHeight:Int = field.params.flatMap(_.js("height").as[Int].toOption).getOrElse(200)

  override def afterRender(): Unit = {
    if(container != null) {

      logger.info(language)

      val value = field.`type` match {
        case JSONFieldTypes.JSON => prop.get.toString()
        case _ => prop.get.string
      }

      val editor = typings.monacoEditor.mod.editor.create(container,IStandaloneEditorConstructionOptions()
        .setLanguage(language)
        .setValue(value)

      )
      editor.onDidChangeModelContent{e =>

        if(field.`type` == JSONFieldTypes.JSON) {
          parse(editor.getValue()) match {
            case Left(_) => prop.set(editor.getValue().asJson)
            case Right(value) => prop.set(value)
          }
        } else {
          prop.set(editor.getValue().asJson)
        }


      }
    }
  }

  override protected def edit(): JsDom.all.Modifier = {

    autoRelease(produce(_id) { _ =>
      container = div(ClientConf.style.editor, height := containerHeight).render


      val title = field.label.getOrElse(field.name)

      //Monaco.load(container,language,prop.get.string,{s:String => prop.set(s.asJson)})
      div(
        h6(title),
        container
      ).render

    })
  }

}

object MonacoWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField) = MonacoWidget(id,field,prop)
}
