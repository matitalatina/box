package ch.wsl.box.client.styles.partials

import ch.wsl.box.client.styles.constants.StyleConstants
import ch.wsl.box.client.styles.fonts.{FontWeight, UdashFonts}
import ch.wsl.box.client.styles.utils.{MediaQueries, StyleUtils}

import scala.language.postfixOps
import scalacss.Defaults._

object FooterStyles extends StyleSheet.Inline {
  import dsl._

  val footer = style(
    borderTop(solid,1 px,black),
    fontSize(1.2 rem),
    color.white,
    padding(15 px),
    MediaQueries.phone(
      style(
        height.auto,
        padding(2 rem, `0`)
      )
    )
  )



}