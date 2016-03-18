package postgresweb.css

import postgresweb.components.{WindowComponent, Tables}

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

