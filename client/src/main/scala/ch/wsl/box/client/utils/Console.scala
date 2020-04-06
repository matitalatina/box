package ch.wsl.box.client.utils

import scala.scalajs.js

object BrowserConsole {
  def log(e:js.Any) = js.Dynamic.global.console.log(e)
}
