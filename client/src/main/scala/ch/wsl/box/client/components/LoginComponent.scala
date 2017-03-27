package ch.wsl.box.client.components

import ch.wsl.box.client.controllers.Controller
import ch.wsl.box.client.css.CommonStyles
import ch.wsl.box.client.services.Auth
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.concurrent.Future
import scalacss.Defaults._
import scalacss.ScalaCssReact._



/**
  * Created by andreaminetti on 17/03/16.
  */
case class LoginComponent(controller:Controller) {

  object Style extends StyleSheet.Inline {
    import dsl._
    val contentCentred = style(
      addClassNames("mdl-layout__content"),
      padding(24.px)
    )

    val topCard = style(addClassNames("mdl-card__title","mdl-color--primary","mdl-color-text--white"))
    val topCardTitle = style(addClassNames("mdl-card__title-text"))
    val cardContent = style(addClassNames("mdl-card__supporting-text"))
    val textFieldDiv = style(addClassNames("mdl-textfield","mdl-js-textfield"))
    val textFieldInput = style(addClassNames("mdl-textfield__input"))
    val textFieldLabel = style(addClassNames("mdl-textfield__label"))


  }

  case class State(username: String, password: String)

  class Backend(scope: BackendScope[Unit, State]) {
    def onChangeUsername(e: ReactEventFromInput) = {
      val value = e.target.value
      e.preventDefaultCB >>
      scope.modState { _.copy(username = value) }
    }

    def onChangePassword(e: ReactEventFromInput) = {
      val value = e.target.value
      e.preventDefaultCB >>
      scope.modState(_.copy(password = value))
    }



    def handleSubmit(e: ReactEventFromInput):Callback = {
      for {
        _ <- Callback.log("logging in")
        _ <- e.preventDefaultCB
        state <- scope.state
        auth <- {
          println(state)
          Auth.login(state.username, state.password)
        }
      } yield auth
    }.void




    def render(state: State) =
      <.div(CommonStyles.layoutCenter,
        <.main(Style.contentCentred,
          <.div(CommonStyles.card,
            <.div(Style.topCard,
              <.h2(Style.topCardTitle,"Box Login")
            ),
            <.div(Style.cardContent,
              <.form(^.onSubmit ==> handleSubmit,
                <.div(Style.textFieldDiv,
                  <.input(Style.textFieldInput, ^.`type` := "text", ^.id := "username", ^.onChange ==> onChangeUsername, ^.value := state.username),
                  <.label(Style.textFieldLabel, ^.`for` := "username", "Username")
                ),
                <.div(Style.textFieldDiv,
                  <.input(Style.textFieldInput, ^.`type` := "password", ^.id := "password", ^.onChange ==> onChangePassword),
                  <.label(Style.textFieldLabel, ^.`for` := "password", "Password")
                )
              )
            ),
            <.div(CommonStyles.action,
              <.button(CommonStyles.button, "Log In", ^.`type` := "submit", ^.onClick ==> handleSubmit)
            )
          )
        )
      )

  }

  val component = ScalaComponent.build[Unit]("LoginComponent")
    .initialState(State("",""))
    .renderBackend[Backend]
    .componentDidMount{ c =>
      Callback{
        scalajs.js.Dynamic.global.componentHandler.upgradeDom()
      }
    }
    .build

  def apply() = component()

}
