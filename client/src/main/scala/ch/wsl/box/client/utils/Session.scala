package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.{IndexState, LoginState}
import org.scalajs.dom
import ch.wsl.box.model.shared.{JSONQuery, IDs}

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by andre on 5/24/2017.
  */
object Session {

  import ch.wsl.box.client.Context._
  import io.circe._
  import io.circe.syntax._
  import io.circe.generic.auto._
  import io.circe.parser._

  final val QUERY = "query"
  final val IDS = "ids"
  final val USER = "user"
  final val AUTH_TOKEN = "auth_token"
  final val LANG = "lang"
  final val LABELS = "labels"

  def set[T](key:String,obj:T)(implicit encoder: Encoder[T]) = {
    println(s"Setting $key")
    dom.window.sessionStorage.setItem(key,obj.asJson.toString())
  }

  def get[T](key:String)(implicit decoder: Decoder[T]):Option[T] = {
    val raw = dom.window.sessionStorage.getItem(key)
    for{
      json <- parse(raw).right.toOption
      query <- json.as[T].right.toOption
    } yield query
  }

  def isSet(key:String):Boolean = {
    Try(dom.window.sessionStorage.getItem(key).size > 0).isSuccess
  }

  import Base64._
  private def basicAuthToken(username: String, password: String):String = "Basic " + Base64.Encoder((username + ":" + password).getBytes).toBase64

  def login(username:String,password:String):Future[Boolean] = {
    dom.window.sessionStorage.setItem(USER,username)
    dom.window.sessionStorage.setItem(AUTH_TOKEN,basicAuthToken(username,password))
    REST.loginCheck().map{ result =>
      io.udash.routing.WindowUrlChangeProvider.changeUrl(IndexState.url)
      dom.window.location.reload()
      true
    }.recover{ case t =>
      dom.window.sessionStorage.removeItem(USER)
      dom.window.sessionStorage.removeItem(AUTH_TOKEN)
      t.printStackTrace()
      false
    }
  }

  def logout() = {
    dom.window.sessionStorage.removeItem(USER)
    dom.window.sessionStorage.removeItem(AUTH_TOKEN)
    io.udash.routing.WindowUrlChangeProvider.changeUrl(LoginState.url)
    dom.window.location.reload()
  }

  def isLogged() = isSet(USER)

  def authToken() = dom.window.sessionStorage.getItem(AUTH_TOKEN)

  def setQuery(query: JSONQuery) = set(QUERY,query)
  def getQuery():Option[JSONQuery] = get[JSONQuery](QUERY)


  def getIDs():Option[IDs] = get[IDs](IDS)
  def setIDs(ids:IDs) = set(IDS,ids)

  def lang():String = Try(dom.window.sessionStorage.getItem(LANG)).toOption match {
    case Some(lang) if Labels.langs.contains(lang)  => lang
    case _ => "en"
  }
  def setLang(lang:String) = {
    dom.window.sessionStorage.setItem(LANG,lang)
    dom.window.location.reload()
  }
}
