package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.JSONField
import ch.wsl.box.rest.model.Form
import ch.wsl.box.rest.service.Auth
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andreaminetti on 10/03/16.
  */
object Forms {
  def list: Future[Seq[String]] = Auth.adminDB.run{
    Form.table.result
  }.map{_.map(_.name)}

  def apply(id:String) = Seq(JSONField("string","a","test"))
}
