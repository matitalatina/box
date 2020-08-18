package ch.wsl.box.client.views


import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.{IndexState, LoginState}
import ch.wsl.box.client.utils.{ClientConf, Labels, Session}
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.form.UdashForm
import org.scalajs.dom.Event

/**
  * Created by andre on 6/7/2017.
  */


case class LoginData(username:String,password:String,message:String)
object LoginData extends HasModelPropertyCreator[LoginData] {
  implicit val blank: Blank[LoginData] =
    Blank.Simple(LoginData("","",""))
}


case object LoginViewPresenter extends ViewFactory[LoginState.type] {
  import ch.wsl.box.client.Context._
  override def create(): (View, Presenter[LoginState.type]) = {
    val model = ModelProperty.blank[LoginData]
    val presenter = LoginPresenter(model)
    (LoginView(model,presenter),presenter)
  }
}

case class LoginPresenter(model:ModelProperty[LoginData]) extends Presenter[LoginState.type] {
  import ch.wsl.box.client.Context._

  override def handleState(state: LoginState.type): Unit = {}

  def login() = {
    Session.login(model.get.username,model.get.password).map{ _ match {
        case true => Unit
        case false => model.subProp(_.message).set(Labels.login.failed)
      }
    }
  }
}

case class LoginView(model:ModelProperty[LoginData],presenter:LoginPresenter) extends View {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import io.udash.css.CssView._


  override def getTemplate = div(
    div(BootstrapStyles.container, height := 400.px)(
      div(BootstrapStyles.Grid.row,
          BootstrapStyles.Flex.justifyContent(BootstrapStyles.FlexContentJustification.Center),
          BootstrapStyles.Flex.alignItems(BootstrapStyles.FlexAlign.Center),
        height := 100.pct
      )(
        div(BootstrapStyles.Card.card,BootstrapStyles.Flex.autoMargin(BootstrapStyles.Side.All))(
          div(BootstrapStyles.Card.header)(
            h3(BootstrapStyles.Card.title)(
              strong(Labels.login.title)
            )
          ),
          div(BootstrapStyles.Card.body)(
            form(
              onsubmit :+= ((e:Event) => {
                e.preventDefault()
                presenter.login()
                false
              }),
              strong(bind(model.subProp(_.message))),
              br,
              label(Labels.login.username),br,
              TextInput(model.subProp(_.username))(width := 300.px),br,br,
              label(Labels.login.password),br,
              PasswordInput(model.subProp(_.password))(width := 300.px),br,br,
              button(BootstrapStyles.Float.right(),ClientConf.style.boxButton,`type` := "submit",Labels.login.button)
            )
          )
        )
      )
    )
  )
}
