package ch.wsl.box.client.services

import ch.wsl.box.client.utils.Base64
import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom
import Base64._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by andreaminetti on 17/03/16.
  */
object Auth {


  def basicAuthToken(username: String, password: String):String = "Basic " + (username + ":" + password).getBytes.toBase64


  private val localStorageAuthKey = "box-auth-token"
  private val localStorageLoggedKey = "box-auth-loggedin"
  private val loggedInTrue = "true"

  def isLoggedIn = dom.window.localStorage.getItem(localStorageLoggedKey) == loggedInTrue

  def auth:(String,String) =  {
    ("Authorization" -> dom.window.localStorage.getItem(localStorageAuthKey))
    //("Authorization" -> basicAuthToken("andreaminetti", ""))
  }

  def login(username:String, password:String):CallbackTo[Future[String]] = CallbackTo{

    println(username)

    dom.window.localStorage.setItem(localStorageAuthKey, basicAuthToken(username, password))

    TableClient.models().map { m => //simple call to check if is logged in
      dom.window.localStorage.setItem(localStorageLoggedKey,loggedInTrue)
      dom.window.location.reload()
      "ok"
    }
  }

  def logout():Callback = Callback{
    dom.window.localStorage.removeItem(localStorageLoggedKey)
    dom.window.localStorage.removeItem(localStorageAuthKey)
  }

}
