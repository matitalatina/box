package ch.wsl.box.rest.utils

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class UserProfile(name: String, db: Database, box:Database) {
  def check = db.run{
    sql"""select 1""".as[Int]
  }.map{ _ =>
    true
  }.recover{case _ => false}
}
