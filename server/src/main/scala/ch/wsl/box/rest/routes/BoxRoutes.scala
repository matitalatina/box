package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.{ActorMaterializer, Materializer}
import ch.wsl.box.model.boxentities._
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
    
    Table[BoxConf.BoxConf,BoxConf.BoxConf_row]("conf",BoxConf.BoxConfTable,isBoxTable = true).route ~
    Table[BoxField.BoxField,BoxField.BoxField_row]("field",BoxField.BoxFieldTable,isBoxTable = true).route ~
    Table[BoxField.BoxField_i18n,BoxField.BoxField_i18n_row]("field_i18n",BoxField.BoxField_i18nTable,isBoxTable = true).route ~
    Table[ch.wsl.box.model.boxentities.BoxForm.BoxForm,ch.wsl.box.model.boxentities.BoxForm.BoxForm_row]("form",ch.wsl.box.model.boxentities.BoxForm.BoxFormTable,isBoxTable = true).route ~
    Table[BoxLabels.BoxLabels,BoxLabels.BoxLabels_row]("labels",BoxLabels.BoxLabelsTable,isBoxTable = true).route ~
    Table[BoxUITable.BoxUI,BoxUITable.BoxUI_row]("ui",BoxUITable.BoxUITable,isBoxTable = true).route

  }
}
           
