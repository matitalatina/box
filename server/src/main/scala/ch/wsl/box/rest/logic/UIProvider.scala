package ch.wsl.box.rest.logic

import ch.wsl.box.model.boxentities.{UITable, UIsrcTable}
import ch.wsl.box.rest.routes.File.BoxFile
import ch.wsl.box.rest.utils.Auth

import scala.concurrent.{ExecutionContext, Future}
import ch.wsl.box.jdbc.PostgresProfile.api._


object UIProvider {
  final val NOT_LOGGED_IN = -1
  def forAccessLevel(accessLevel:Int)(implicit ec:ExecutionContext):Future[Map[String,String]] = Auth.boxDB.run {
    UITable.table.filter(_.accessLevel <= accessLevel).result
  }.map { result =>
    result.groupBy(_.key).map{ case (key,values) =>
      key -> values.sortBy(-_.accessLevel).head.value
    }
  }

  def fileForAccessLevel(name:String,accessLevel:Int)(implicit ec:ExecutionContext):Future[Option[BoxFile]] = Auth.boxDB.run {
    val q = UIsrcTable.table.filter(t => t.accessLevel <= accessLevel && t.name === name)
    val r = q.result
    r
  }.map { _.headOption.map{ uiFile =>
    BoxFile(uiFile.file,uiFile.mime,uiFile.name)
  }}
}


