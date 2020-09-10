package ch.wsl.box.client.styles.constants
import ch.wsl.box.client.styles.utils.StyleUtils
import scalacss.ProdDefaults._

object Font extends StyleSheet.Inline {
  import dsl._

  var regular = StyleUtils.unsafeProp("font-family","SelawikRegular")
  var bold = StyleUtils.unsafeProp("font-family","SelawikBold")

}
