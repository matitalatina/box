package ch.wsl.box.rest.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, HttpOrigin, `Content-Disposition`}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import ch.wsl.box.model.BoxActionsRegistry
import ch.wsl.box.rest.logic._
import ch.wsl.box.model.boxentities.{Conf, Schema, UITable}
import ch.wsl.box.rest.utils.{BoxConf, BoxSession}
import ch.wsl.box.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, ExecutionContext, Future}
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import ch.wsl.box.model.shared.{EntityKind, LoginRequest}
import ch.wsl.box.rest.jdbc.JdbcConnect
import ch.wsl.box.rest.logic.functions.RuntimeFunction
import ch.wsl.box.rest.metadata.{BoxFormMetadataFactory, EntityMetadataFactory, FormMetadataFactory, StubMetadataFactory}
import ch.wsl.box.rest.pdf.Pdf
import ch.wsl.box.rest.runtime.Registry
import com.softwaremill.session.{InMemoryRefreshTokenStorage, SessionConfig, SessionManager}
import com.typesafe.config.Config
import scribe.Logging
import akka.http.scaladsl.server.directives.CachingDirectives._
import ch.wsl.box.rest.Box
import ch.wsl.box.rest.routes.v1.ApiV1
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.util.{Failure, Success}

/**
  * Created by andreaminetti on 15/03/16.
  */
case class Root(akkaConf:Config, restart: () => Unit, origins:Seq[String])(implicit materializer:Materializer,executionContext:ExecutionContext,system: ActorSystem) extends Logging {


  lazy val sessionConfig = SessionConfig.fromConfig(akkaConf)
  implicit lazy val sessionManager = new SessionManager[BoxSession](sessionConfig)
  implicit lazy val refreshTokenStorage = new InMemoryRefreshTokenStorage[BoxSession] {
    override def log(msg: String): Unit = {}
  }

  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._
  import ch.wsl.box.rest.utils.Auth
  import io.circe.generic.auto._
  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JSONUtils._

  def status = path("status") {
    cachingProhibited {
      complete(
        HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,"RUNNING"))
      )
    }
  }

  def ddl = pathPrefix("ddl") {
    path("box") {
      complete(
        HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,
          Schema.box.createStatements.mkString("\n\n\n")
            .replaceAll(",",",\n\t")
            .replaceAll("\" \\(", "\" (\n\t")
        ))
      )
    }
  }

  def resetServer = pathPrefix("server") {
    path("reset") {
      restart()
      complete(
        HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,"restart server"))
      )
    }
  }

  def resetCache = pathPrefix("cache") {
    path("reset") {
      FormMetadataFactory.resetCache()
      EntityMetadataFactory.resetCache()
      RuntimeFunction.resetCache()
      BoxConf.load()
      complete(
        HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,"reset cache"))
      )
    }
  }


  val settings = CorsSettings.defaultSettings
    .withAllowGenericHttpRequests(false)
    .withAllowedOrigins(HttpOriginMatcher.strict(origins.map(x => HttpOrigin(x)):_*))
    .withAllowedMethods(List(HttpMethods.GET,HttpMethods.POST,HttpMethods.PUT,HttpMethods.DELETE))

  val route:Route = UI.clientFiles ~
    status ~
    ddl ~
    resetServer ~
    resetCache ~
    cors() {
      ApiV1().route
    }


}


