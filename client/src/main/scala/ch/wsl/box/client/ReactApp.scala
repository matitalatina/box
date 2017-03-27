package ch.wsl.box.client

import ch.wsl.box.client.css.AppCSS
import ch.wsl.box.client.routes.AppRouter
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object ReactApp extends JSApp {

  @JSExport
  override def main(): Unit = {
    AppCSS.load
    //loads all the routes
    AppRouter.router().renderIntoDOM(dom.document.getElementById("box-app"))

  }

}

