package ch.wsl.box.client.views


import ch.wsl.box.client.{IndexState, LoginState}
import ch.wsl.box.client.utils.Session
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.form.UdashForm
import org.scalajs.dom.Event

/**
  * Created by andre on 6/7/2017.
  */


case class LoginData(username:String,password:String,message:String)

case object LoginViewPresenter extends ViewPresenter[LoginState.type] {
  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[LoginState.type]) = {
    val model = ModelProperty{
      LoginData("","","")
    }
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
        case false => model.subProp(_.message).set("Login failed")
      }
    }
  }
}

case class LoginView(model:ModelProperty[LoginData],presenter:LoginPresenter) extends View {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate = div(
    div(BootstrapStyles.container)(
      div(BootstrapStyles.Panel.panelDefault)(
        div(BootstrapStyles.Panel.panelHeading)(
          h3(BootstrapStyles.Panel.panelTitle)(
            strong("Sign In")
          )
        ),
        div(BootstrapStyles.Panel.panelBody)(
          strong(bind(model.subProp(_.message))),
          br,
          UdashForm.textInput()("Username")(model.subProp(_.username)),
          UdashForm.passwordInput()("Password")(model.subProp(_.password)),
          button(`type` := "submit",BootstrapStyles.Button.btn,onclick :+= ((e:Event) => presenter.login()),"Login")
        )
      )
    )
  )
}
