package ch.wsl.box.rest.routes.v1

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix}
import akka.stream.Materializer
import ch.wsl.box.model.BoxActionsRegistry
import ch.wsl.box.model.shared.EntityKind
import ch.wsl.box.rest.metadata.{BoxFormMetadataFactory, StubMetadataFactory}
import ch.wsl.box.rest.routes.{BoxFileRoutes, BoxRoutes, Form, Table}
import ch.wsl.box.rest.utils.{Auth, BoxSession, UserProfile}
import com.softwaremill.session.SessionManager

import scala.concurrent.ExecutionContext

case class Admin(session:BoxSession)(implicit ec:ExecutionContext, userProfile: UserProfile, mat:Materializer, system:ActorSystem) {

  import Directives._
  import ch.wsl.box.rest.utils.Auth
  import ch.wsl.box.rest.utils.JSONSupport._
  import io.circe.generic.auto._

  def forms = pathPrefix("box-admin") {
    pathPrefix(Segment) { lang =>
      pathPrefix(Segment) { name =>
        Form(name, lang,BoxActionsRegistry().tableActions,BoxFormMetadataFactory(),userProfile.boxDb,EntityKind.BOX.kind).route
      }
    } ~ pathEnd{
      complete(BoxFormMetadataFactory().list)
    }
  }

  def createStub = pathPrefix("create-stub"){
    pathPrefix(Segment) { entity =>
      complete(StubMetadataFactory.forEntity(entity))
    }
  }

  def file = pathPrefix("boxfile") {
    BoxFileRoutes.route(session.userProfile.boxUserProfile, mat, ec)
  }

  def entity = pathPrefix("boxentity") {
    BoxRoutes()(session.userProfile.boxUserProfile, mat, ec)
  }

  def entities = path("boxentities") {
    get {
      complete(Table.boxTables.toSeq.sorted)
    }
  }



  val route = Auth.onlyAdminstrator(session) { //need to be at the end or non administrator request are not resolved
    //access to box tables for administrator
    forms ~
    createStub  ~
    file  ~
    entity   ~
    entities
  }
}
