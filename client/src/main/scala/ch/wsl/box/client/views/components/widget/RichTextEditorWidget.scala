package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.services.BrowserConsole
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
import typings.quill.mod.{DeltaStatic, Quill, QuillOptionsStatic, Sources}

import scala.collection.mutable
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
    logger.debug(s"data: ${prop.get.toString()}")
    produce(_id) { _ =>
      val container = div( height := 300.px).render
      val parent = div(container).render


      //Line break fix inspired by:
      // https://codepen.io/farnabaz/pen/VpdaGW
      // https://codepen.io/mackermedia/pen/gmNwZP

      val Parchment = typings.quill.mod.default.`import`("parchment").asInstanceOf[js.Dynamic]
      val Delta = typings.quill.mod.default.`import`("delta").asInstanceOf[js.Dynamic]
      val Break = typings.quill.mod.default.`import`("blots/break").asInstanceOf[js.Dynamic]
      val Embed = typings.quill.mod.default.`import`("blots/embed").asInstanceOf[js.Dynamic]
      val Block = typings.quill.mod.default.`import`("blots/block").asInstanceOf[js.Dynamic]

      val lineBreakMatcher:js.Function0[js.Any] = () => {
        val newDelta = js.Dynamic.newInstance(Delta)()
        newDelta.insert(js.Dynamic.literal(break = ""))
        newDelta
      }


      val handleEnter = js.ThisFunction.fromFunction3((self:js.Dynamic,range:typings.quill.mod.RangeStatic,context:js.Any) => {
        if (range.length > 0) {
          self.quill.scroll.deleteAt(range.index, range.length);  // So we do not trigger text-change
        }

        val contextFormat = context.asInstanceOf[js.Dynamic].format.asInstanceOf[js.Dictionary[js.Any]]


        val lineFormats:js.Dictionary[js.Any] = js.Object.keys(contextFormat.asInstanceOf[js.Object]).foldLeft(js.Dictionary[js.Any]())((lineFormats:js.Dictionary[js.Any], format) => {
          if (Parchment.query(format, Parchment.Scope.BLOCK).asInstanceOf[Boolean] && !js.Array.isArray(contextFormat(format))) {
            lineFormats(format) = contextFormat(format);
          }
          lineFormats
        })


        self.quill.insertText(range.index, "\n", lineFormats, Sources.user)
        self.quill.setSelection(range.index + 1, Sources.silent)



      })


      val handleLinebreak = js.ThisFunction.fromFunction3((self:js.Dynamic,range:typings.quill.mod.RangeStatic,context:js.Any) => {
        self.quill.insertEmbed(range.index, "break", true, Sources.user)
        val nextChar = self.quill.getText(range.index + 2, 1)

        logger.debug(s"nextChar: $nextChar, length: ${nextChar.asInstanceOf[String].length}")
        if(nextChar.asInstanceOf[String].length > 0 && nextChar.charCodeAt(0).asInstanceOf[Int] == 10) {
          logger.debug(s"nextCharCode: ${nextChar.charCodeAt(0).asInstanceOf[Int]}")
        }

        if(nextChar.asInstanceOf[String].length == 0) {
          self.quill.insertEmbed(range.index, "break", true, Sources.user)
        }
        self.quill.setSelection(range.index + 1, Sources.silent);
      })

      val options = QuillOptionsStatic()
        .setPlaceholder(field.placeholder.getOrElse(""))
        .setTheme("snow")
        .setDebug("debug")
        .setModules(StringDictionary(
          "toolbar" -> toolbar,
          "clipboard" -> js.Dictionary("matchers" -> js.Array(js.Array("BR",lineBreakMatcher))),
          "keyboard" -> js.Dynamic.literal(
            "bindings" -> js.Dynamic.literal(
              "handleEnter" -> js.Dynamic.literal(
                "key" -> 13,
                "handler" -> handleEnter
              ),
              "linebreak" -> js.Dynamic.literal(
                "key" -> 13,
                "shiftKey" -> true,
                "handler" -> handleLinebreak
              )
            )
          )
        ))

      Break.prototype.insertInto = js.ThisFunction.fromFunction3((self:js.Any,parent:js.Any, ref:js.Any) => {
        Embed.prototype.insertInto.call(self, parent, ref)
      })

      Break.prototype.length= () => 1
      Break.prototype.value= () => "\n"
//      Break.prototype.blotName = "break"
//      Break.prototype.tagName = "BR"


      val editor = new typings.quill.mod.default(container,options)

      editor.root.innerHTML = prop.get.string

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