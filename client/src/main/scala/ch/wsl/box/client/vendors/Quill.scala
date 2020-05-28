package ch.wsl.box.client.vendors

import org.scalajs.dom.raw.HTMLElement

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

  def load(el:HTMLElement,value:String, placeholder:String, onChange:String => Unit) = {
    js.Dynamic.global.console.log(el)

    js.Dynamic.global.require(js.Array("quill"),{Quill:js.Dynamic => {
      val quill:js.Dynamic = js.Dynamic.newInstance(Quill)(el,js.Dictionary(
        "modules" -> js.Dictionary(
          "toolbar" -> js.Array(js.Array("bold", "italic", "underline"))
        ),
        "placeholder" -> placeholder,
        "theme" -> "snow"
      ))
      val delta = quill.clipboard.convert(value)
      quill.setContents(delta,"silent")

      quill.on("text-change",{ () => {
        onChange(quill.root.innerHTML.asInstanceOf[String])
      }}: js.Function)

    }}:js.Function1[js.Dynamic,Unit])




  }


}
