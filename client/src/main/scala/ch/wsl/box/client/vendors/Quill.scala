package ch.wsl.box.client.vendors

import ch.wsl.box.client.views.components.widget.RichTextEditorWidget.{Full, Minimal, Mode}
import scala.concurrent.duration._
import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js.timers._
import scala.concurrent.duration._

import scala.scalajs.js

object Quill {

  /**
   * var quill = new Quill('#editor-container', {
   * modules: {
   * toolbar: [
   * ['bold', 'italic', 'underline']
   * ]
   * },
   * placeholder: 'Compose an epic...',
   * theme: 'snow'  // or 'bubble'
   * });
   */



  def load(el:HTMLElement,value:String, placeholder:String, mode:Mode,  onChange:String => Unit) = {
    js.Dynamic.global.console.log(el)

    val toolbar = mode match {
      case Minimal => js.Array(
        js.Array("bold", "italic", "underline","strike"),
        js.Array("clean")
      )
      case Full => js.Array(
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


    js.Dynamic.global.require(js.Array("quill"),{Quill:js.Dynamic => {
      val quill:js.Dynamic = js.Dynamic.newInstance(Quill)(el,js.Dictionary(
        "modules" -> js.Dictionary(
          "toolbar" -> toolbar
        ),
        "placeholder" -> placeholder,
        "theme" -> "snow",
        "debug" -> "debug"
      ))


      setTimeout(500.milli) {
        val delta = quill.clipboard.convert(value)
        quill.setContents(delta, "silent")
      }

      quill.on("text-change",{ () => {
          onChange(quill.root.innerHTML.asInstanceOf[String])
      }}: js.Function)

    }}:js.Function1[js.Dynamic,Unit])




  }


}
