package ch.wsl.box.client.utils

import scala.scalajs.js

/**
  * Created by andreaminetti on 19/10/16.
  */
object Log {
  def console(obj:js.Any) = js.Dynamic.global.console.log(obj)
}
