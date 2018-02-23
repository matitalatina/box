package ch.wsl.box.rest.logic

import ch.wsl.box.rest.boxentities.UITable
import ch.wsl.box.rest.utils.Auth

import scala.concurrent.{ExecutionContext, Future}
import slick.driver.PostgresDriver.api._


object UIProvider {
  final val NOT_LOGGED_IN = -1
  def forAccessLevel(accessLevel:Int)(implicit ec:ExecutionContext):Future[Map[String,String]] = Auth.boxDB.run {
    UITable.table.filter(_.accessLevel <= accessLevel).result
  }.map { result =>
    result.groupBy(_.key).map{ case (key,values) =>
      key -> values.sortBy(-_.accessLevel).head.value
    }
  }
}


