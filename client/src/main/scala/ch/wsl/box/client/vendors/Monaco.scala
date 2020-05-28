package ch.wsl.box.client.vendors
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js

object Monaco {
  def load(el:HTMLElement,language:String, value:String,onChange:String => Unit) = {
    js.Dynamic.global.require(js.Array("vs/editor/editor.main"),{() => {
      val editor = js.Dynamic.global.monaco.editor.create(el,js.Dictionary(
        "value" -> value,
        "language" -> language
      ))
      editor.onDidChangeModelContent({(e) => {
        onChange(editor.getValue().asInstanceOf[String])
      }}:js.Function1[js.Dynamic,Unit])

    }}:js.Function0[Unit])
  }
}
