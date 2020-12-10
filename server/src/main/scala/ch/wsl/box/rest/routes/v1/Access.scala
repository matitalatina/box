package ch.wsl.box.rest.routes.v1

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import ch.wsl.box.rest.logic.TableAccess
import ch.wsl.box.rest.utils.{Auth, BoxSession}
import io.circe._, io.circe.generic.auto._, io.circe.syntax._

import scala.concurrent.ExecutionContext

case class Access(session:BoxSession)(implicit ec:ExecutionContext) {

  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._

  val route = pathPrefix("access") {
    pathPrefix("box-admin") {
      pathPrefix(Segment) { table =>
        path("table-access") {
          complete(TableAccess(table,Auth.boxDbSchema,session.username,Auth.boxDB).map(_.asJson))
        }
      }
    } ~
      pathPrefix("table" | "view" | "entity" | "form") {
        pathPrefix(Segment) { table =>
          path("table-access") {
            complete(TableAccess(table,Auth.dbSchema,session.username,Auth.adminDB).map(_.asJson))
          }
        }
      }
  }

}
