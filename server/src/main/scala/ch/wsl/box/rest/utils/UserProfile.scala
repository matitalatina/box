package ch.wsl.box.rest.utils

import ch.wsl.box.rest.boxentities.User
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}

case class UserProfile(name: String, db: Database, boxDb:Database) {

  def check(implicit ec:ExecutionContext): Future[Boolean] = db.run{
    sql"""select 1""".as[Int]
  }.map{ _ =>
    true
  }.recover{case _ => false}

  def accessLevel(implicit ec:ExecutionContext):Future[Int] = Auth.boxDB.run{
    User.table.filter(_.username === name).result
  }.map(_.headOption.map(_.access_level_id).getOrElse(-1))

  def boxUserProfile = UserProfile(name, boxDb, boxDb)   //todo : do it less ugly
}
