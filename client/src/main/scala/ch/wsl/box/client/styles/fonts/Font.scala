package ch.wsl.box.client.styles.fonts

import ch.wsl.box.client.styles.utils.StyleUtils
import scalacss.ProdDefaults._
import scalacss.internal.CssEntry.FontFace

object Font extends StyleSheet.Inline {

  import dsl._

  val name = "Open Sans"


  val bold = style(
    StyleUtils.unsafeProp("font-family",name),
    fontWeight._700
  )

  val regular = style(
    StyleUtils.unsafeProp("font-family",name),
    fontWeight._600
  )


}
