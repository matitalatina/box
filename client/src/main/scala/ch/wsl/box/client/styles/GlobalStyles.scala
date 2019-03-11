package ch.wsl.box.client.styles



import ch.wsl.box.client.styles.constants.StyleConstants
import ch.wsl.box.client.styles.constants.StyleConstants.Colors
import ch.wsl.box.client.styles.utils.MediaQueries

import scala.language.postfixOps
import scalacss.ProdDefaults._
import scalacss.internal.{AV, CanIUse}
import scalacss.internal.CanIUse.Agent
import scalacss.internal.LengthUnit.px
import scalatags.JsDom
import scalatags.generic.Attr


case class StyleConf(colors:Colors)


case class GlobalStyles(conf:StyleConf) extends StyleSheet.Inline {

  private def unsafeProp(key:String,value:String) = AV(scalacss.internal.Attr.real(key),value)

  import dsl._

  val inputDefaultWidth = width(50 %%)

  val global = style(

    unsafeRoot("body") (
      backgroundColor.white
    ),

    unsafeRoot("select")(
      inputDefaultWidth,
      borderStyle.solid,
      borderWidth(1 px),
      borderRadius.`0`,
      borderColor(rgb(169, 169, 169)),
      height(21 px)
    ),

    unsafeRoot("input")(
      inputDefaultWidth,
      borderStyle.solid,
      borderWidth(1 px),
      borderRadius.`0`,
      backgroundColor.white,
      borderColor(rgb(169, 169, 169)),
      height(21 px),
      paddingLeft(5 px),
      paddingRight(5 px),
    ),

    unsafeRoot("input[type='checkbox']")(
      width.auto,
      height.auto
    ),

    unsafeRoot("input[type='number']")(
      textAlign.right
    ),

    unsafeRoot("input[type='file']")(
      width(100 %%),
      height.auto,
      borderWidth(0 px),
      backgroundColor.transparent
    ),

    unsafeRoot("textarea")(
      inputDefaultWidth,
      borderStyle.solid,
      borderWidth(1 px),
      borderRadius.`0`,
      backgroundColor.white,
      borderColor(rgb(169, 169, 169)),
      resize.vertical
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
      backgroundColor(conf.colors.main)
    ),

    unsafeRoot("footer")(
      borderTop(conf.colors.main,5 px,solid),
      backgroundColor.white,
      overflow.hidden,
      fontSize(1.2 rem),
      color.darkgray,
      padding(15 px),
      height(55 px)
    ),

    unsafeRoot(".form-control")( // this controls the datetime input
      paddingTop(1 px),
      paddingBottom(1 px),
      paddingRight(5 px),
      textAlign.left,
      lineHeight(14 px),
      height(21 px),
      borderStyle.solid,
      borderWidth(1 px),
      borderRadius.`0`,
      backgroundColor.white,
      borderColor(rgb(169, 169, 169))
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
        color(conf.colors.main)
      ),
      color(conf.colors.main)
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

  val dateTimePicker = style(
    inputDefaultWidth,
    textAlign.right,
    float.right
  )

  val dateTimePickerFullWidth = style(
    width(100 %%),
    textAlign.right,
    float.right
  )

  val smallCells = style(
    padding(3 px).important,
    fontSize(10 px),
    unsafeRoot("p")(
      margin(0 px)
    )
  )

  val numberCells = style(
    textAlign.right,
    paddingRight(3 px)
  )

  val textCells = style(
    textAlign.left,
    paddingLeft(3 px)
  )

  val lookupCells = style(
    textAlign.center
  )

  val dateCells = style(
    textAlign.center
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
    top(40 px),
    right(40 px),
    zIndex(2000)
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
      color(conf.colors.link)
    ),
    color(conf.colors.link),
    textTransform.uppercase,
    fontWeight.bold
  )

  val contentMinHeight = style(
    minHeight(400 px),
    marginTop.`0`
  )

  val fullHeightMax = style(
    maxHeight :=! "calc(100vh - 206px)",
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
    padding(10 px),
    border(1 px,solid,StyleConstants.Colors.GreySemi),
    backgroundColor(StyleConstants.Colors.GreyExtra)
  )

  val boxButton = style(
    whiteSpace.nowrap,
    height(22 px),
    padding(3 px, 7 px),
    fontSize(12 px),
    lineHeight(12 px),
    margin(3 px, 1 px),
    border(1 px,solid,conf.colors.main),
    color(conf.colors.main),
    backgroundColor(white),
    &.hover(
      color(white),
      backgroundColor(conf.colors.main)
    ),
    &.attrExists("disabled") (
      backgroundColor(lightgray),
      color(gray),
      borderColor(gray)
    )
  )

  val boxNavigationLabel = style(
    textAlign.center,
    lineHeight(26 px)
  )


  val boxButtonImportant = style(
    whiteSpace.nowrap,
    height(22 px),
    padding(3 px, 7 px),
    fontSize(12 px),
    lineHeight(12 px),
    margin(3 px, 1 px),
    border(1 px,solid,conf.colors.main),
    backgroundColor(conf.colors.main),
    color.white,
    &.hover(
      backgroundColor.white,
      color(conf.colors.main)
    )
  )

  val boxButtonDanger = style(
    whiteSpace.nowrap,
    height(22 px),
    padding(3 px, 7 px),
    fontSize(12 px),
    lineHeight(12 px),
    margin(3 px, 1 px),
    border(1 px,solid,conf.colors.danger),
    backgroundColor(conf.colors.danger),
    color.white,
    &.hover(
      backgroundColor.white,
      color(conf.colors.danger)
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
    width(260 px),
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
    backgroundColor(conf.colors.main),
    zIndex(10),
    textAlign.right,
    unsafeChild("a") {
      color(conf.colors.link)
    }
  )

  val hrThin = style(
    marginTop(2 px),
    marginBottom(10 px)
  )

  val labelRequired = style(
    fontWeight.bolder
  )

  val notNullable = style(
//    borderColor(StyleConstants.Colors.orange),
//    borderStyle.solid,
//    borderWidth(2 px)
  )

  val smallLabelRequired = style(
    fontSize(8.px),
    color(conf.colors.danger)
  )

  val labelNonRequred = style(
    fontWeight.bold
  )

//  val fixedHeader = style(
//    unsafeRoot("tbody")(
////    display.block,
//    overflow.auto,
//      maxHeight :=! "calc(100vh - 330px)",
////    height(200 px),
////    width(100 %%)
//  ),
//    unsafeRoot("thead")(
////    display.block
//  )
//  )


}