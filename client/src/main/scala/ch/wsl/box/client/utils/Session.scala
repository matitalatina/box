package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.{Context, IndexState, LoginState}
import org.scalajs.dom
import ch.wsl.box.model.shared.{IDs, JSONQuery, LoginRequest}

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
  final val LANG = "lang"
  final val LABELS = "labels"
  final val STATE = "state"

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

  def login(username:String,password:String):Future[Boolean] = {
    dom.window.sessionStorage.setItem(USER,username)
    REST.login(LoginRequest(username,password)).map{ result =>
      if(Option(dom.window.sessionStorage.getItem(STATE)).isDefined) {
        val state = dom.window.sessionStorage.getItem(STATE)
        io.udash.routing.WindowUrlChangeProvider.changeUrl(state)
        dom.window.sessionStorage.removeItem(STATE)
      } else {
        io.udash.routing.WindowUrlChangeProvider.changeUrl(IndexState.url)
      }
      dom.window.location.reload()
      true
    }.recover{ case t =>
      dom.window.sessionStorage.removeItem(USER)
      t.printStackTrace()
      false
    }
  }


  def logoutAndSaveState() = {
    dom.window.sessionStorage.setItem(STATE,Context.applicationInstance.currentState.url)
    logout()
  }

  def logout() = {
    dom.window.sessionStorage.removeItem(USER)
    REST.logout().map{ result =>
      io.udash.routing.WindowUrlChangeProvider.changeUrl(LoginState.url)
    }
  }

  def isLogged() = isSet(USER)

  def getQuery():Option[JSONQuery] = get[JSONQuery](QUERY)
  def setQuery(query: JSONQuery) = set(QUERY,query)
  def resetQuery() = set(QUERY, None)


  def getIDs():Option[IDs] = get[IDs](IDS)
  def setIDs(ids:IDs) = set(IDS, ids)
  def resetIDs() = set(IDS, None)

  def lang():String = Try(dom.window.sessionStorage.getItem(LANG)).toOption match {
    case Some(lang) if Labels.langs.contains(lang)  => lang
    case _ => "en"
  }
  def setLang(lang:String) = {
    Labels.load(lang)
    dom.window.sessionStorage.setItem(LANG,lang)
    dom.window.location.reload()
  }
}
