package ch.wsl.box.client.css

import ch.wsl.box.client.components.{WindowComponent, Tables}
import ch.wsl.box.client.components.WindowComponent

import scalacss.ScalaCssReact._
import scalacss.mutable.GlobalRegistry
import scalacss.Defaults._

object AppCSS {

  def load = {
    GlobalRegistry.register(
      GlobalStyle,
      Tables.Style,
      WindowComponent.Style,
      CommonStyles
    )
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}

