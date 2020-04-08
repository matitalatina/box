package ch.wsl.box.model.boxentities

import ch.wsl.box.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */
object AccessLevel {

  val profile = ch.wsl.box.jdbc.PostgresProfile


  case class AccessLevel_row(access_level_id: Int, access_level:String)

  class AccessLevel(_tableTag: Tag) extends profile.api.Table[AccessLevel_row](_tableTag, "access_level") {
    def * = (access_level_id,access_level) <> (AccessLevel_row.tupled, AccessLevel_row.unapply)

    val access_level_id: Rep[Int] = column[Int]("access_level_id", O.PrimaryKey)
    val access_level: Rep[String] = column[String]("access_level")

  }
  /** Collection-like TableQuery object for table Conf  */
  lazy val table = new TableQuery(tag => new AccessLevel(tag))

}
