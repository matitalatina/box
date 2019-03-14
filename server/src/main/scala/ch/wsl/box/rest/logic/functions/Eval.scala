package ch.wsl.box.rest.logic.functions

import scala.reflect.runtime.currentMirror

object Eval {

  import scala.tools.reflect.ToolBox

  def apply[A](string: String): A = {
    val toolbox = currentMirror.mkToolBox()
    val tree = toolbox.parse(string)
    toolbox.eval(tree).asInstanceOf[A]
  }



}
