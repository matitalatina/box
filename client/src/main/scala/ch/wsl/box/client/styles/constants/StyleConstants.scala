package ch.wsl.box.client.styles.constants
import scalacss.Defaults._

object StyleConstants extends StyleSheet.Inline{
  import dsl._

  /**
    * SIZES
    */
  object Sizes {
    val BodyWidth = 1075

    val MinSiteHeight = 550

    val HeaderHeight = 80

    val HeaderHeightMobile = 80

    val FooterHeight = 120

    val MobileMenuButton = 50
  }

  /**
    * COLORS
    */
  object Colors {
    val Red = c"#e30613"

    val RedLight = c"#ff2727"

    val GreyExtra = c"#fafafa"

    val GreySemi = c"#cfcfd6"

    val Grey = c"#777785"

    val Yellow = c"#ffd600"

    val Cyan = c"#eef4f7"

    val bordeaux = c"#4c1c24"
    val wsl = c"#006268"
    val wslLink = c"#fbf0b2"

    val orange = c"#ffa500"
  }

  /**
    * MEDIA QUERIES
    */
  object MediaQueriesBounds {
    val TabletLandscapeMax = Sizes.BodyWidth - 1

    val TabletLandscapeMin = 768

    val TabletMax = TabletLandscapeMin - 1

    val TabletMin = 481

    val PhoneMax = TabletMin - 1
  }
}