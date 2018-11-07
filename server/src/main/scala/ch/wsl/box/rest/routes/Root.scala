package ch.wsl.box.rest.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.boxentities.{Conf, UITable}
import ch.wsl.box.rest.utils.{BoxConf, BoxSession}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, ExecutionContext, Future}
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import ch.wsl.box.model.shared.LoginRequest
import ch.wsl.box.rest.jdbc.JdbcConnect
import com.softwaremill.session.{InMemoryRefreshTokenStorage, SessionConfig, SessionManager}
import com.typesafe.config.Config
import scribe.Logging

import scala.util.{Failure, Success}

/**
  * Created by andreaminetti on 15/03/16.
  */
trait Root extends Logging {

  implicit val materializer:Materializer
  implicit val executionContext:ExecutionContext

  val binding: Future[Http.ServerBinding]
  val system: ActorSystem

  val akkaConf:Config

  lazy val sessionConfig = SessionConfig.fromConfig(akkaConf)
  implicit lazy val sessionManager = new SessionManager[BoxSession](sessionConfig)
  implicit lazy val refreshTokenStorage = new InMemoryRefreshTokenStorage[BoxSession] {
    override def log(msg: String): Unit = {}
  }

  def boxSetSession(v: BoxSession) = setSession(oneOff, usingCookies, v)


  def stop() = {
    binding
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate())
  }

  val route:Route = {

    import Directives._
    import ch.wsl.box.rest.utils.JSONSupport._
    import ch.wsl.box.rest.utils.Auth
    import io.circe.generic.auto._
    import io.circe.syntax._
    import ch.wsl.box.shared.utils.JsonUtils._


    //Serving UI
    UI.clientFiles ~
    pathPrefix("webjars") {
      WebJarsSupport.webJars
    } ~
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
                complete(BoxConf.clientConf)
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
            path("ui") {
              get {
                optionalSession(oneOff, usingCookiesOrHeaders) {
                  case None => complete(UIProvider.forAccessLevel(UIProvider.NOT_LOGGED_IN))
                  case Some(session) => complete(for {
                    accessLevel <- session.userProfile.accessLevel
                    ui <- UIProvider.forAccessLevel(accessLevel)
                  } yield ui)
                }
              }
            } ~
            pathPrefix("uiFile") {
              path(Segment) { fileName =>
                get {
                  optionalSession(oneOff, usingCookiesOrHeaders) { session =>
                    val boxFile = session match {
                      case None => UIProvider.fileForAccessLevel(fileName,UIProvider.NOT_LOGGED_IN)
                      case Some(session) => for {
                        accessLevel <- session.userProfile.accessLevel
                        ui <- UIProvider.fileForAccessLevel(fileName,accessLevel)
                      } yield ui
                    }
                    onSuccess(boxFile){
                      case Some(f) => File.completeFile(f)
                      case None => complete(StatusCodes.NotFound,"Not found")
                    }
                  }
                }
              }
            } ~
            touchRequiredSession(oneOff, usingCookiesOrHeaders) { session =>
              implicit val up = session.userProfile
              implicit val db = up.db
//              val accessLevel = session.userProfile.accessLevel.get

              pathPrefix("access") {
                pathPrefix("table") {
                  pathPrefix(Segment) { table =>
                    path("write") {
                      complete(TableAccess.write(table,Auth.dbSchema,session.username))
                    }
                  }
                }
              } ~
              pathPrefix("export") {
                Export.route
              } ~
              pathPrefix("file") {
                FileRoutes.route
              } ~
              pathPrefix("entity") {
                pathPrefix(Segment) { lang =>
                  GeneratedRoutes(lang)
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
                    Form(name, lang).route
                  }
                }
              } ~
              pathPrefix("news") {
                pathPrefix(Segment) { lang =>
                    get{
                      complete(NewsLoader.get(lang))
                    }
                }
              } ~
              Auth.onlyAdminstrator(session) { //need to be at the end or non administrator request are not resolved
                //access to box tables for administrator
                pathPrefix("boxfile") {
                  BoxFileRoutes.route(session.userProfile.boxUserProfile, materializer, executionContext)
                } ~
                  pathPrefix("boxentity") {
                    BoxRoutes()(session.userProfile.boxUserProfile, materializer, executionContext)
                  } ~
                  path("boxentities") {
                    get {
                      complete(Table.boxTables.toSeq.sorted)
                    }
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


