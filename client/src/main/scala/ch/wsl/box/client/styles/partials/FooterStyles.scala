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
    height(StyleConstants.Sizes.FooterHeight px),
    fontSize(1.2 rem),
    color.black,

    MediaQueries.phone(
      style(
        height.auto,
        padding(2 rem, `0`)
      )
    )
  )

  val footerInner = style(
    StyleUtils.relativeMiddle,

    MediaQueries.phone(
      style(
        top.auto,
        transform := "none"
      )
    )
  )



}