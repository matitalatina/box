package ch.wsl.box.client.styles



import scala.language.postfixOps
import scalacss.Defaults._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  val global = style(
    unsafeRoot("header")(
      clear.both,
      height(50 px),
      padding(10 px),
      borderBottom(1 px,solid,black),
      marginBottom(20 px)
    )
  )

  val smallTable = style(
    unsafeRoot("td")(
      padding(1 px)
    )
  )

}