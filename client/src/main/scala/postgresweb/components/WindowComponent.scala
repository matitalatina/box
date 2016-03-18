package postgresweb.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.Auth
import postgresweb.components.navigation.{TopNav, LeftNav}
import postgresweb.controllers.Controller
import postgresweb.css.CommonStyles

import scala.concurrent.Future
import scala.scalajs.js
import scalacss.Defaults._
import scalacss.ScalaCssReact._


object WindowComponent {

  object Style extends StyleSheet.Inline {
    import dsl._
    val content = style(addClassNames("mdl-layout__content"))

    val pageContent = style(addClassNames("page-content"))

  }


  def login(e: ReactEventI): CallbackTo[Future[String]] = {
    Auth.login("","")
  }

  def render(P:Props):ReactElement = {
    if(Auth.isLoggedIn) {
      renderOk(P)
    } else {
      renderLogin(P)
    }
  }

  def renderLogin(P:Props):ReactElement = LoginComponent(P.controller)()

  def renderOk(P:Props) = {

    println("rendering window")

    <.div(CommonStyles.layout,
      TopNav(TopNav.Props(P.controller)),
      LeftNav(LeftNav.Props(P.controller)),
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


  val component = ReactComponentB[Props]("ItemsPage")
    .render_P(render)
    .build

  case class Props(controller: Controller)

  def apply(props : Props,ref: js.UndefOr[String] = "", key: js.Any = {}) = component.set(key, ref)(props)

}
