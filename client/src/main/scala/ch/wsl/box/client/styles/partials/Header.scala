package ch.wsl.box.client.styles.partials
import java.util.concurrent.TimeUnit

import ch.wsl.box.client.styles.constants.StyleConstants
import ch.wsl.box.client.styles.utils.{MediaQueries, StyleUtils}

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps
import scalacss.Defaults._

object HeaderStyles extends StyleSheet.Inline {
  import dsl._

  val header = style(
    position.relative,
    backgroundColor.black,
    height(StyleConstants.Sizes.HeaderHeight px),
    fontSize(1.6 rem),
    zIndex(99),

    MediaQueries.tabletPortrait(
      style(
        height(StyleConstants.Sizes.HeaderHeight * .7 px)
      )
    )
  )

  val headerLeft = style(
    position.relative,
    float.left,
    color.white,
    height(100 %%)
  )

  val headerRight = style(
    position.relative,
    float.right,
    height(100 %%)
  )


}