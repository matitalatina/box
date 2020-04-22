package ch.wsl.box.testmodel

import ch.wsl.box.rest.runtime._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import ch.wsl.box.rest.utils.UserProfile


object GeneratedRoutes extends GeneratedRoutes {

  import Entities._
  import ch.wsl.box.rest.routes._
  import ch.wsl.box.rest.utils.JSONSupport._
  import Directives._
  import io.circe.generic.auto._

  def apply(lang: String)(implicit up: UserProfile, mat: Materializer, ec: ExecutionContext):Route = {
  implicit val db = up.db

    Table[Child,Child_row]("child",Child, lang).route ~ 
    Table[Parent,Parent_row]("parent",Parent, lang).route ~ 
    Table[Simple,Simple_row]("simple",Simple, lang).route ~ 
    Table[Subchild,Subchild_row]("subchild",Subchild, lang).route
  }
}
           
