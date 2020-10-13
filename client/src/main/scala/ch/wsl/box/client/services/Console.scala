package ch.wsl.box.client.services

import scala.scalajs.js

object BrowserConsole {
  def log(e:js.Any) = js.Dynamic.global.console.log(e)
}
