package ch.wsl.box.client.styles



import scala.language.postfixOps
import scalacss.Defaults._
import scalacss.internal.LengthUnit.px

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  val inputDefaultWidth = width(70 %%)

  val global = style(

    unsafeRoot("select")(
      inputDefaultWidth
    ),

    unsafeRoot("input")(
      inputDefaultWidth
    ),

    unsafeRoot("textarea")(
      inputDefaultWidth
    ),

    unsafeRoot("header")(
      clear.both,
      height(50 px),
      padding(10 px),
      borderBottom(1 px,solid,black),
      marginBottom(20 px)
    ),

    unsafeRoot(".form-control")(
      inputDefaultWidth,
      paddingTop(1 px),
      paddingBottom(1 px),
      paddingRight(5 px),
      textAlign.right,
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
    padding.`0`,
    paddingRight(10 px),
    paddingLeft(10 px)
  )

  val field = style(
    padding.`0`,
    marginBottom(5 px)
  )


}