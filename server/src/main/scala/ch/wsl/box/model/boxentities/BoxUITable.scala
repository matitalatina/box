package ch.wsl.box.model.boxentities

import ch.wsl.box.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */
object BoxUITable {

  val profile = ch.wsl.box.jdbc.PostgresProfile


  case class BoxUI_row(id: Option[Int] = None, key:String, value: String, accessLevel:Int)

  class BoxUI(_tableTag: Tag) extends profile.api.Table[BoxUI_row](_tableTag, "ui") {
    def * = (Rep.Some(id), key, value, accessLevel) <> (BoxUI_row.tupled, BoxUI_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id),  key, value, accessLevel).shaped.<>({r=>import r._; _1.map(_=> BoxUI_row.tupled((_1, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val key: Rep[String] = column[String]("key")
    val value: Rep[String] = column[String]("value")
    val accessLevel: Rep[Int] = column[Int]("access_level_id")

  }
  /** Collection-like TableQuery object for table Form */
  lazy val BoxUITable = new TableQuery(tag => new BoxUI(tag))

}
