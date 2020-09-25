package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.utils.{BrowserConsole, ClientConf}
import ch.wsl.box.client.views.components.widget.RichTextEditorWidget.Mode
import ch.wsl.box.model.shared.JSONField
import ch.wsl.box.shared.utils.JSONUtils._
import io.circe.Json
import io.circe.syntax._
import io.udash._
import io.udash.properties.single.Property
import org.scalablytyped.runtime.StringDictionary
import scalatags.JsDom
import scribe.Logging
import typings.quill.mod.{DeltaStatic, QuillOptionsStatic, Sources}

import scala.scalajs.js
import scala.util.Try

case class RichTextEditorWidget(_id: Property[Option[String]], field: JSONField, prop: Property[Json], mode:Mode) extends Widget with Logging {
  import scalacss.ScalatagsCss._
  import scalatags.JsDom.all._

  val toolbar = mode match {
    case RichTextEditorWidget.Minimal => js.Array(
      js.Array("bold", "italic", "underline","strike"),
      js.Array("clean")
    )
    case RichTextEditorWidget.Full => js.Array(
      js.Array("bold", "italic", "underline","strike"),
      js.Array("blockquote", "code-block"),
      js.Array(js.Dictionary("script" -> "sub"),js.Dictionary("script" -> "super")),
      js.Array(js.Dictionary("list" -> "ordered"),js.Dictionary("list" -> "bullet")),
      js.Array(js.Dictionary("size" -> js.Array("small",false,"large","huge"))),
      js.Array(js.Dictionary("header" -> js.Array(1,2,3,4,5,6,false))),
      js.Array(js.Dictionary("color" -> js.Array()),js.Dictionary("background" -> js.Array())),
      js.Array(js.Dictionary("align" -> js.Array())),
      js.Array("link","image"),
      js.Array("clean")

    )
  }


  override protected def show(): JsDom.all.Modifier = autoRelease(produce(prop){ p =>
    div(p.string).render
  })

  _id.listen(x => logger.info(s"Rich text widget load with ID: $x"))

  override protected def edit(): JsDom.all.Modifier = {
    logger.debug(s"field: ${field.name} widget mode $mode")
    produce(_id) { _ =>
      val container = div( height := 300.px).render
      val parent = div(container).render
      BrowserConsole.log(typings.quill.mod.default)


      val editor = new typings.quill.mod.default(container
        ,QuillOptionsStatic()
        .setPlaceholder(field.placeholder.getOrElse(""))
        .setTheme("snow")
        .setDebug("debug")
        .setModules(StringDictionary(
          "toolbar" -> toolbar
        ))
      )

      val delta = editor.clipboard.convert(prop.get.string)
      editor.setContents(delta, Sources.silent)

      editor.on_textchange(typings.quill.quillStrings.`text-change`,
        (delta:DeltaStatic,oldContent:DeltaStatic,source:Sources) => prop.set(editor.root.innerHTML.asJson)
      )

      div(
        parent
      ).render

    }
  }

}

object RichTextEditorWidget {
  sealed trait Mode
  case object Minimal extends Mode
  case object Full extends Mode
}

case class RichTextEditorWidgetFactory(mode:Mode) extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField) = RichTextEditorWidget(id,field,prop,mode)
}