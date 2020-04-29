package ch.wsl.box.rest.routes.v1

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import ch.wsl.box.rest.logic.TableAccess
import ch.wsl.box.rest.utils.{Auth, BoxSession}

import scala.concurrent.ExecutionContext

case class Access(session:BoxSession)(implicit ec:ExecutionContext) {

  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._

  val route = pathPrefix("access") {
    pathPrefix("box-admin") {
      pathPrefix(Segment) { table =>
        path("write") {
          complete(TableAccess.write(table,Auth.boxDbSchema,session.username,Auth.boxDB))
        }
      }
    } ~
      pathPrefix("table" | "view" | "entity" | "form") {
        pathPrefix(Segment) { table =>
          path("write") {
            complete(TableAccess.write(table,Auth.dbSchema,session.username,Auth.adminDB))
          }
        }
      }
  }

}
