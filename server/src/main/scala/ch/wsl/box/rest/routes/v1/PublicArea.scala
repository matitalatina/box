package ch.wsl.box.rest.routes.v1

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import ch.wsl.box.model.boxentities.BoxPublicEntities
import ch.wsl.box.rest.utils.Auth
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.runtime.Registry

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class PublicArea(implicit ec:ExecutionContext, mat:Materializer, system:ActorSystem) {

  lazy val publicEntities:Future[Seq[BoxPublicEntities.Row]] = Auth.boxDB.run(BoxPublicEntities.table.result)

  import akka.http.scaladsl.server.Directives._

  implicit val up = Auth.adminUserProfile

  val route:Route = pathPrefix("public") {
    pathPrefix(Segment) { entity =>
      val route: Future[Route] = publicEntities.map{ pe =>
        pe.find(_.entity == entity).flatMap(e => Registry().actions.actions(e.entity)) match {
          case Some(action) => EntityRead(entity,action)
          case None => complete(StatusCodes.NotFound,"Entity not found")
        }
      }
      onComplete(route) {
        case Success(value) => value
        case Failure(e) => {
          e.printStackTrace()
          complete(StatusCodes.InternalServerError,"error")
        }
      }
    }
  }


}
