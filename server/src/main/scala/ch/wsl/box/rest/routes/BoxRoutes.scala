package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.{ActorMaterializer, Materializer}
import ch.wsl.box.rest.boxentities._
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ch.wsl.box.rest.utils.UserProfile

import scala.concurrent.ExecutionContext

object BoxRoutes {
  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._
  import io.circe.generic.auto._

  def apply()(implicit up:UserProfile, mat:Materializer, ec: ExecutionContext):Route = {
    import io.circe.generic.auto._
    implicit val db = up.db
    
    Table[Conf.Conf,Conf.Conf_row]("conf",Conf.table,isBoxTable = true).route ~
    Table[Field.Field,Field.Field_row]("field",Field.table,isBoxTable = true).route ~
    Table[Field.Field_i18n,Field.Field_i18n_row]("field_i18n",Field.Field_i18n,isBoxTable = true).route ~
    Table[ch.wsl.box.rest.boxentities.Form.Form,ch.wsl.box.rest.boxentities.Form.Form_row]("form",ch.wsl.box.rest.boxentities.Form.table,isBoxTable = true).route ~
    Table[Labels.Labels,Labels.Labels_row]("labels",Labels.table,isBoxTable = true).route ~
    Table[UITable.UI,UITable.UI_row]("ui",UITable.table,isBoxTable = true).route

  }
}
           
