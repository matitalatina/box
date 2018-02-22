package ch.wsl.box.rest.logic

import ch.wsl.box.rest.boxentities.UITable
import ch.wsl.box.rest.utils.Auth

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global

object UIProvider {
  final val NOT_LOGGED_IN = -1
  def forAccessLevel(accessLevel:Int):Future[Map[String,String]] = Auth.boxDB.run {
    UITable.table.filter(_.accessLevel <= accessLevel).result
  }.map { result =>
    result.groupBy(_.key).map{ case (key,values) =>
      key -> values.sortBy(-_.accessLevel).head.value
    }
  }
}


