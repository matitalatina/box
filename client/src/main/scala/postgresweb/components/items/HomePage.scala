package postgresweb.components.items

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object HomePage {

  val component = ReactComponentB.static("Item1",
    <.div(
      <.h1("Home Page")
    )
  ).buildU

  def apply() = component()
}
