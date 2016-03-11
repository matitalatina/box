package postgresweb

import org.scalajs.dom
import postgresweb.css.AppCSS
import postgresweb.routes.AppRouter

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ReactApp extends JSApp {

  @JSExport
  override def main(): Unit = {
    AppCSS.load
    AppRouter.router().render(dom.document.body)
  }

}

