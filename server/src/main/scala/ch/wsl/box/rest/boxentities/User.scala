package ch.wsl.box.rest.boxentities

import ch.wsl.box.rest.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */

object User {

  val profile = ch.wsl.box.rest.jdbc.PostgresProfile


  case class User_row(username:String, access_level_id: Int)

  class User(_tableTag: Tag) extends profile.api.Table[User_row](_tableTag, "users") {
    def * = (username,access_level_id) <> (User_row.tupled, User_row.unapply)


    val username: Rep[String] = column[String]("username")
    val access_level_id: Rep[Int] = column[Int]("access_level_id")

  }
  /** Collection-like TableQuery object for table Form */
  lazy val table = new TableQuery(tag => new User(tag))

}