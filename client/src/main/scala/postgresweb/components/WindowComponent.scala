package postgresweb.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.components.navigation.{TopNav, LeftNav}
import postgresweb.controllers.Controller
import postgresweb.css.CommonStyles

import scala.scalajs.js
import scalacss.Defaults._
import scalacss.ScalaCssReact._

object WindowComponent {

  object Style extends StyleSheet.Inline {
    import dsl._
    val content = style(addClassNames("mdl-layout__content"))
    val pageContent = style(addClassNames("page-content"))
  }

  val component = ReactComponentB[Props]("ItemsPage")
    .render_P { P =>
      <.div(CommonStyles.layout,
        TopNav(TopNav.Props(P.controller)),
        LeftNav(LeftNav.Props("Tables",P.controller)),
        <.main(Style.content,
          <.div(Style.pageContent,
            <.div(CommonStyles.row,
              <.div(CommonStyles.fullWidth,
                P.controller.render()
              )
            )
          )
        )
      )
    }
    .build

  case class Props(controller: Controller)

  def apply(props : Props,ref: js.UndefOr[String] = "", key: js.Any = {}) = component.set(key, ref)(props)

}
