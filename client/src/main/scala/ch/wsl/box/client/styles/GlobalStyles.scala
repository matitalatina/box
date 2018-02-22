package ch.wsl.box.client.styles



import ch.wsl.box.client.styles.constants.StyleConstants

import scala.language.postfixOps
import scalacss.Defaults._
import scalacss.internal.LengthUnit.px

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  val inputDefaultWidth = width(50 %%)

  val global = style(

    unsafeRoot("body") (
      backgroundColor(StyleConstants.Colors.wsl)
    ),

    unsafeRoot("select")(
      inputDefaultWidth
    ),

    unsafeRoot("input")(
      inputDefaultWidth
    ),

    unsafeRoot("input[type='checkbox']")(
      width.auto
    ),

    unsafeRoot("input[type='number']")(
      textAlign.right
    ),

    unsafeRoot("textarea")(
      inputDefaultWidth
    ),

    unsafeRoot("select")(
      direction.rtl
    ),

    unsafeRoot("option")(
      direction.ltr
    ),

    unsafeRoot("header")(
      clear.both,
      height(50 px),
      padding(10 px),
      borderBottom(1 px,solid,black),
      color.white
    ),

    unsafeRoot(".form-control")(
      paddingTop(1 px),
      paddingBottom(1 px),
      paddingRight(5 px),
      textAlign.left,
      lineHeight(14 px),
      borderRadius.`0`,
      height(21 px)
    ),

    unsafeRoot(".container-fluid")(
      padding.`0`
    ),

    unsafeRoot("main")(
      paddingLeft(30 px),
      paddingRight(30 px),
      backgroundColor.white
    )
  )


  val smallCells = style(
    padding(3 px).important,
    fontSize(10 px),
    unsafeRoot("p")(
      margin(0 px)
    )
  )

  val noPadding = style( padding.`0` )
  val smallBottomMargin = style( marginBottom(5 px) )

  val subBlock = style(
    padding.`0`
  )

  val block = style(
    paddingTop.`0`,
    paddingBottom.`0`,
    paddingRight(20 px),
    paddingLeft(20 px)
  )

  val field = style(
    padding.`0`
  )


  val notificationArea = style(
    position.fixed,
    top(20 px),
    right(20 px)
  )

  val notification = style(
    padding(20 px),
    border(1 px,solid,red),
    backgroundColor.white
  )

  val headerLogo = style(
    height(39 px),
    marginTop(-5 px),
    marginBottom(-5 px),
    marginLeft(0 px),
    marginRight(10 px)
  )

  val linkHeaderFooter = style(
    color(StyleConstants.Colors.wslLink)
  )

  val contentMinHeight = style(
    minHeight(400 px),
    marginTop.`0`
  )

  val fullHeightMax = style(
    maxHeight :=! "calc(100vh - 230px)",
    overflow.auto
  )

  val noMargin = style(
    margin.`0`
  )

  val subform = style(
    marginTop(-1 px),
    padding(15 px),
    border(1 px,solid,StyleConstants.Colors.GreySemi),
    backgroundColor(StyleConstants.Colors.GreyExtra)
  )

  val boxButton = style(
    padding(3 px, 7 px),
    fontSize(12 px),
    lineHeight(28 px),
    border(1 px,solid,StyleConstants.Colors.wsl),
    color(StyleConstants.Colors.wsl)
  )

  val largeButton = style(
    inputDefaultWidth
  )

}