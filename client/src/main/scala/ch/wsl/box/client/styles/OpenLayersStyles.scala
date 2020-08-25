package ch.wsl.box.client.styles

import scalacss.ProdDefaults._

object OpenLayersStyles extends StyleSheet.Inline {

  import dsl._

  val olStyle = style(
    unsafeRoot(".ol-control button")(
      backgroundColor(c"rgba(108, 117, 125, 0.8)"),
      &.hover(
        backgroundColor(c"#5a6268"),
      )
    )
  )
}
