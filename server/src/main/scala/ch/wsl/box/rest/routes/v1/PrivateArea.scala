package ch.wsl.box.rest.routes.v1

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix}
import akka.stream.Materializer
import ch.wsl.box.model.BoxActionsRegistry
import ch.wsl.box.model.shared.EntityKind
import ch.wsl.box.rest.logic.NewsLoader
import ch.wsl.box.rest.metadata.{BoxFormMetadataFactory, FormMetadataFactory, StubMetadataFactory}
import ch.wsl.box.rest.routes.{BoxFileRoutes, BoxRoutes, Export, Form, Functions, Table, View}
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.rest.utils.{Auth, BoxSession, UserProfile}
import com.softwaremill.session.SessionDirectives.touchRequiredSession
import com.softwaremill.session.SessionManager
import com.softwaremill.session.SessionOptions.{oneOff, usingCookiesOrHeaders}

import scala.concurrent.ExecutionContext

case class PrivateArea(implicit ec:ExecutionContext, sessionManager: SessionManager[BoxSession], mat:Materializer, system:ActorSystem) {

  import Directives._
  import ch.wsl.box.rest.utils.Auth
  import ch.wsl.box.rest.utils.JSONSupport._
  import io.circe.generic.auto._

  def export(implicit up:UserProfile) = pathPrefix("export") {
    Export.route
  }

  def function(implicit up:UserProfile) = pathPrefix("function") {
    Functions.route
  }

  def file(implicit up:UserProfile) = pathPrefix("file") {
    Registry().fileRoutes()
  }

  def entity(implicit up:UserProfile) = pathPrefix("entity") {
    pathPrefix(Segment) { lang =>
      Registry().routes(lang)
    }
  }

  def entities = path("entities") {
    get {
      val alltables = Table.tables ++ View.views
      complete(alltables.toSeq.sorted)
    }
  }

  def tables = path("tables") {
    get {
      complete(Table.tables.toSeq.sorted)
    }
  }

  def views = path("views") {
    get {
      complete(View.views.toSeq.sorted)
    }
  }

  def forms(implicit up:UserProfile) = path("forms") {
    get {
      complete(FormMetadataFactory(Auth.boxDB,Auth.adminDB).list)
    }
  }

  def form(implicit up:UserProfile) = pathPrefix("form") {
    pathPrefix(Segment) { lang =>
      pathPrefix(Segment) { name =>
        Form(name, lang,x => Registry().actions(x),FormMetadataFactory(Auth.boxDB,Auth.adminDB),up.db,EntityKind.FORM.kind).route
      }
    }
  }

  def news(implicit up:UserProfile) = pathPrefix("news") {
    pathPrefix(Segment) { lang =>
      get{
        complete(NewsLoader.get(lang))
      }
    }
  }

  val route = touchRequiredSession(oneOff, usingCookiesOrHeaders) { session =>
    implicit val up = session.userProfile
    implicit val db = up.db

    Access(session).route ~
      export ~
      function ~
      file ~
      entity ~
      entities ~
      tables ~
      views ~
      forms ~
      form ~
      news ~
      Admin(session).route
  }
}
