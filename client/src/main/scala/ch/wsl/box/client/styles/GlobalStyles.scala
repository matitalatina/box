package ch.wsl.box.client.styles



import ch.wsl.box.client.styles.constants.StyleConstants
import ch.wsl.box.client.styles.utils.MediaQueries

import scala.language.postfixOps
import scalacss.Defaults._
import scalacss.internal.{AV, CanIUse}
import scalacss.internal.CanIUse.Agent
import scalacss.internal.LengthUnit.px
import scalatags.JsDom
import scalatags.generic.Attr

object GlobalStyles extends StyleSheet.Inline {

  private def unsafeProp(key:String,value:String) = AV(scalacss.internal.Attr.real(key),value)

  import dsl._

  val inputDefaultWidth = width(50 %%)

  val global = style(

    unsafeRoot("body") (
      backgroundColor.white
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

    unsafeRoot("input[type='file']")(
      width(100 %%)
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
      lineHeight(29 px),
      borderBottom(1 px,solid,black),
      color.white,
      backgroundColor(StyleConstants.Colors.wsl)
    ),

    unsafeRoot("footer")(
      borderTop(StyleConstants.Colors.wsl,5 px,solid),
      backgroundColor.white,
      overflow.hidden,
      fontSize(1.2 rem),
      color.darkgray,
      padding(15 px),
      height(55 px)
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
    ),

    unsafeRoot("a")(
      &.hover(
        color(StyleConstants.Colors.wsl)
      ),
      color(StyleConstants.Colors.wsl)
    ),

    unsafeRoot("#box-table table")(
      backgroundColor.white
    ),


    //hide up/down arrow for input
    unsafeRoot("input[type=\"number\"]::-webkit-outer-spin-button,\n  input[type=\"number\"]::-webkit-inner-spin-button")(
      unsafeProp("-webkit-appearance","none"),
      margin.`0`
    ),

    unsafeRoot("input[type=\"number\"]")(
      unsafeProp("-moz-appearance","textfield")
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
    height(40 px),
    maxWidth( 100 %%),
    marginTop(-10 px),
    marginBottom(-5 px),
    marginLeft(0 px),
    marginRight(10 px)
  )

  val linkHeaderFooter = style(
    &.hover(
      color(StyleConstants.Colors.wslLink)
    ),
    color(StyleConstants.Colors.wslLink),
    textTransform.uppercase,
    fontWeight.bold
  )

  val contentMinHeight = style(
    minHeight(400 px),
    marginTop.`0`
  )

  val fullHeightMax = style(
    maxHeight :=! "calc(100vh - 230px)",
    overflow.auto
  )

  val fullHeight = style(
    height :=! "calc(100vh - 105px)",
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
    whiteSpace.nowrap,
    padding(3 px, 7 px),
    fontSize(12 px),
    lineHeight(28 px),
    border(1 px,solid,StyleConstants.Colors.wsl),
    color(StyleConstants.Colors.wsl),
    &.hover(
      color(StyleConstants.Colors.wsl)
    )
  )

  val boxButtonImportant = style(
    whiteSpace.nowrap,
    padding(3 px, 7 px),
    fontSize(12 px),
    lineHeight(28 px),
    border(1 px,solid,StyleConstants.Colors.wsl),
    backgroundColor(StyleConstants.Colors.wsl),
    color.white,
    &.hover(
      color.white
    )
  )

  val boxButtonDanger = style(
    whiteSpace.nowrap,
    padding(3 px, 7 px),
    fontSize(12 px),
    lineHeight(28 px),
    border(1 px,solid,StyleConstants.Colors.bordeaux),
    backgroundColor(StyleConstants.Colors.bordeaux),
    color.white,
    &.hover(
      color.white
    )
  )

  val largeButton = style(
    inputDefaultWidth
  )

  val fullWidth = style(
    width(100 %%)
  )

  val maxFullWidth = style(
    maxWidth(100 %%).important
  )

  val imageThumb = style(
    height.auto,
    width(100 %%)
  )

  val noBullet = style(
    listStyleType := "none"
  )

  val navigationArea = style(
    paddingLeft(20 px),
    paddingRight(20 px),
    paddingTop(5 px),
    paddingBottom(5 px)
  )

  val navigatorArea = style(
    width(180 px),
    textAlign.right
  )

  val noMobile = style(
    media.maxWidth(600 px)(
      display.none
    )
  )

  val mobileOnly = style(
    display.none,
    media.maxWidth(600 px)(
      display.block
    )
  )

  val mobileMenu = style(
    position.absolute,
    padding(10 px),
    top(50 px),
    left.`0`,
    width(100 %%),
    backgroundColor(StyleConstants.Colors.wsl),
    zIndex(10),
    textAlign.right,
    unsafeChild("a") {
      color(StyleConstants.Colors.wslLink)
    }
  )







}