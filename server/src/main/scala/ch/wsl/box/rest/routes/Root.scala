package ch.wsl.box.rest.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import ch.wsl.box.rest.logic.{JSONFormMetadataFactory, LangHelper}
import ch.wsl.box.rest.boxentities.Conf
import ch.wsl.box.rest.utils.BoxSession
import com.softwaremill.session.{InMemoryRefreshTokenStorage, SessionConfig, SessionManager}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session._
import BoxSession._
import akka.actor.ActorSystem
import ch.wsl.box.model.shared.LoginRequest

import scala.util.Success
/**
  * Created by andreaminetti on 15/03/16.
  */
trait
Root {

  implicit val materializer:Materializer
  implicit val executionContext:ExecutionContext



  val sessionConfig = SessionConfig.fromConfig()
  implicit val sessionManager = new SessionManager[BoxSession](sessionConfig)
  implicit val refreshTokenStorage = new InMemoryRefreshTokenStorage[BoxSession] {
    override def log(msg: String): Unit = {}
  }

  def boxSetSession(v: BoxSession) = setSession(oneOff, usingCookies, v)


  val route:Route = {

    import Directives._
    import ch.wsl.box.rest.utils.JSONSupport._
    import ch.wsl.box.rest.utils.Auth
    import Auth.PostgresAuthenticator._
    import io.circe.generic.auto._


    //Serving UI
    UI.clientFiles ~
      //Serving REST-API
      pathPrefix("api" / "v1") {
        //version 1
        //cors() {
        // enable cross domain usage
        //          options {
        //            complete(StatusCodes.OK)
        //          } ~
        pathPrefix("labels") {
          path(Segment) { lang =>
            get {
              complete(LangHelper(lang).translationTable)
            }
          }
        } ~
          path("conf") {
            get {
              complete(Auth.boxDB.run {
                Conf.table.result
              }.map { result =>
                result.map(x => x.key -> x.value).toMap
              }
              )
            }
          } ~
          path("logout") {
            get {
              invalidateSession(oneOff, usingCookies) {
                complete("ok")
              }
            }
          } ~
          path("login") {
            post {
              entity(as[LoginRequest]) { request =>
                val usernamePassword = BoxSession.fromLogin(request)
                onComplete(usernamePassword.userProfile.check) {
                  case Success(true) => boxSetSession(usernamePassword) {
                    complete("ok")
                  }
                  case _ => complete(StatusCodes.Unauthorized, "nok")
                }
              }
            }
          } ~
          touchRequiredSession(oneOff, usingCookiesOrHeaders) { session =>
            implicit val db = session.userProfile.db

            pathPrefix("file") {
              FileRoutes.route
            } ~
              pathPrefix("entity") {
                pathPrefix(Segment) { lang =>
                  GeneratedRoutes()
                }
              } ~
              path("entities") {
                get {
                  val alltables = Table.tables ++ View.views
                  complete(alltables.toSeq.sorted)
                }
              } ~
              path("tables") {
                get {
                  complete(Table.tables.toSeq.sorted)
                }
              } ~
              path("views") {
                get {
                  complete(View.views.toSeq.sorted)
                }
              } ~
              path("forms") {
                get {
                  complete(JSONFormMetadataFactory().list)
                }
              } ~
              pathPrefix("form") {
                pathPrefix(Segment) { lang =>
                  pathPrefix(Segment) { name =>
                    Form(name, lang)
                  }
                }
              }
          }
      }
//        ~
//              touchRequiredSession {  userProfile =>
//              implicit val boxdb = userProfile.db
//                path("box") {
//                    get {
//                      val alltables = Table.tables ++ View.views
//                      complete(alltables.toSeq.sorted)
//                    }
//                }
//            }
//        }
      //}
  }
}


