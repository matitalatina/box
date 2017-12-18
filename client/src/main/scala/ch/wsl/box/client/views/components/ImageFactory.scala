package ch.wsl.box.client.views.components
import ch.wsl.box.client.utils.Conf
import org.scalajs.dom

import scalatags.JsDom

class ImageFactory(prefix: String) {
  import scalatags.JsDom.all._
  def apply(name: String, altText: String, xs: Modifier*): JsDom.TypedTag[dom.html.Image] = {
    img(src := s"$prefix/$name", alt := altText, height := Conf.imageHeight, xs)
  }
}

object Image extends ImageFactory("assets/images")