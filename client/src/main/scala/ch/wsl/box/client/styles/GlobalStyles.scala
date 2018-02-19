package ch.wsl.box.client.styles



import scala.language.postfixOps
import scalacss.Defaults._
import scalacss.internal.LengthUnit.px

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  val inputDefaultWidth = width(50 %%)

  val global = style(

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
      marginBottom(20 px)
    ),

    unsafeRoot(".form-control")(
      paddingTop(1 px),
      paddingBottom(1 px),
      paddingRight(5 px),
      textAlign.left,
      lineHeight(14 px),
      borderRadius.`0`,
      height(21 px)
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

  val subBlock = style(
    padding.`0`
  )

  val block = style(
    paddingTop.`0`,
    paddingBottom.`0`,
    paddingRight(50 px),
    paddingLeft(10 px)
  )

  val field = style(
    padding.`0`,
    marginBottom(5 px)
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

}