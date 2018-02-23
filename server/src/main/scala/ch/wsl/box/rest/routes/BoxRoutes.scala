package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import ch.wsl.box.rest.boxentities._

import scala.concurrent.ExecutionContext

object BoxRoutes {
  import Directives._
  import ch.wsl.box.rest.utils.JSONSupport._
  import io.circe.generic.auto._

  def apply()(implicit db:slick.driver.PostgresDriver.api.Database, mat:Materializer, ec: ExecutionContext):Route = {
    Table[Conf.Conf,Conf.Conf_row]("conf",Conf.table).route ~
    Table[Field.Field,Field.Field_row]("field",Field.table).route ~
    Table[Field.Field_i18n,Field.Field_i18n_row]("field",Field.Field_i18n).route ~
    Table[ch.wsl.box.rest.boxentities.Form.Form,ch.wsl.box.rest.boxentities.Form.Form_row]("form",ch.wsl.box.rest.boxentities.Form.table).route ~
    Table[Labels.Labels,Labels.Labels_row]("labels",Labels.table).route
  }
}
           
