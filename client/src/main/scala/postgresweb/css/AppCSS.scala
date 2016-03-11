package postgresweb.css

import postgresweb.components.items.Tables

import scalacss.ScalaCssReact._
import scalacss.mutable.GlobalRegistry
import scalacss.Defaults._

object AppCSS {

  def load = {
    GlobalRegistry.register(
      GlobalStyle,
      Tables.Style,
      CommonStyles
    )
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}

