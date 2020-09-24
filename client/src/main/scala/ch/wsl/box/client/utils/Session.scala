package ch.wsl.box.client.utils

import ch.wsl.box.client.services.{Navigate, REST}
import ch.wsl.box.client.{Context, IndexState, LoginState}
import org.scalajs.dom
import ch.wsl.box.model.shared.{IDs, JSONQuery, LoginRequest}
import io.udash.properties.single.Property
import scribe.Logging

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by andre on 5/24/2017.
  */

case class SessionQuery(query:JSONQuery, entity:String)
object SessionQuery{
  def empty = SessionQuery(JSONQuery.empty,"")
}

object Session extends Logging {

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
  final private val BASE_LAYER = "base_layer"

  lazy val logged = {
    Property(false)
  }

  isValidSession().map{
    case true => logged.set(true)
    case false => logout()
  }

  def set[T](key:String,obj:T)(implicit encoder: Encoder[T]) = {
    logger.info(s"Setting $key")
    dom.window.sessionStorage.setItem(key,obj.asJson.toString())
  }

  def get[T](key:String)(implicit decoder: Decoder[T]):Option[T] = {
    val raw = dom.window.sessionStorage.getItem(key)
    for{
      json <- parse(raw).right.toOption
      query <- json.as[T].right.toOption
    } yield query
  }

  def isValidSession():Future[Boolean] = {
    isSet(USER) match {
      case false => Future.successful(false)
      case true => REST.validSession()
    }
  }

  def isSet(key:String):Boolean = {
    Try(dom.window.sessionStorage.getItem(key).size > 0).isSuccess
  }

  def login(username:String,password:String):Future[Boolean] = {
    dom.window.sessionStorage.setItem(USER,username)
    val fut = for{
      _ <- REST.login(LoginRequest(username,password))
      _ <- UI.load()
    } yield {
      logged.set(true)
      if(Option(dom.window.sessionStorage.getItem(STATE)).isDefined && dom.window.sessionStorage.getItem(STATE).trim.length > 0) {
        val state = dom.window.sessionStorage.getItem(STATE).replaceAll("#","")
        logger.info(s"navigate to $state")
        Navigate.toUrl(state)
        dom.window.sessionStorage.removeItem(STATE)
      } else {
        dom.window.sessionStorage.removeItem(STATE)
        Navigate.to(IndexState)
      }
      true
    }

    fut.recover{ case t =>
      dom.window.sessionStorage.removeItem(USER)
      dom.window.sessionStorage.removeItem(STATE) // don't persist state if something is wrong
      logged.set(false)
      t.printStackTrace()
      false
    }
  }


  def logoutAndSaveState() = {
    Try{
      dom.window.sessionStorage.setItem(STATE,Context.applicationInstance.currentState.url)
    }
    logout()
  }

  def logout() = {
    Navigate.toAction{ () =>
      dom.window.sessionStorage.removeItem(USER)
      for{
        _ <- REST.logout()
        _ <- UI.load()
      } yield {
        logged.set(false)
        Navigate.to(LoginState)
      }
    }
  }

  def getQuery():Option[SessionQuery] = get[SessionQuery](QUERY)
  def setQuery(query: SessionQuery) = set(QUERY,query)
  def resetQuery() = set(QUERY, None)

  def getBaseLayer():Option[String] = get[String](BASE_LAYER)
  def setBaseLayer(bl: String) = set(BASE_LAYER,bl)


  def getIDs():Option[IDs] = get[IDs](IDS)
  def setIDs(ids:IDs) = set(IDS, ids)
  def resetIDs() = set(IDS, None)

  def lang():String = Try(dom.window.sessionStorage.getItem(LANG)).toOption match {
    case Some(lang) if ClientConf.langs.contains(lang)  => lang
    case _ if ClientConf.langs.nonEmpty => ClientConf.langs.head
    case _ => "en"
  }
  def setLang(lang:String) = {
    Labels.load(lang)
    dom.window.sessionStorage.setItem(LANG,lang)
    dom.window.location.reload()
  }
}
