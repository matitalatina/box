package postgresweb.css

import japgolly.scalajs.react.vdom.TagMod

import scalacss.Defaults._
import scalacss.ScalaCssReact._

/**
 * Created by andreaminetti on 22/02/16.
 */
object CommonStyles extends StyleSheet.Inline {

  import dsl._

  val layout:TagMod = style(addClassNames("mdl-layout","mdl-js-layout","mdl-layout--fixed-drawer","mdl-layout--fixed-header","mdl-layout--fixed-tabs"))

  val spacer:TagMod = style(addClassNames("mdl-layout-spacer"))
  val navigation:TagMod = style(addClassNames("mdl-navigation"))
  val navigationLink:TagMod = style(addClassNames("mdl-navigation__link"))

  val title:TagMod = style(addClassNames("mdl-layout-title"))

  val row:TagMod = style(
    addClassNames("mdl-grid"),
    margin(0.px),
    padding(0.px)
  )
  val fullWidth:TagMod = style(
    addClassNames("mdl-cell","mdl-cell--12-col"),
    margin(0.px),
    width(100.%%)
  )

  val card:TagMod = style(addClassNames("mdl-card","mdl-shadow--2dp"))

  val scroll:TagMod = style(overflow.auto)

}
