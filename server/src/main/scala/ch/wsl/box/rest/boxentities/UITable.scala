package ch.wsl.box.rest.boxentities

import ch.wsl.box.rest.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */
object UITable {

  val profile = ch.wsl.box.rest.jdbc.PostgresProfile


  case class UI_row(id: Option[Int] = None, key:String, value: String, accessLevel:Int)

  class UI(_tableTag: Tag) extends profile.api.Table[UI_row](_tableTag, "ui") {
    def * = (Rep.Some(id), key, value, accessLevel) <> (UI_row.tupled, UI_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id),  key, value, accessLevel).shaped.<>({r=>import r._; _1.map(_=> UI_row.tupled((_1, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val key: Rep[String] = column[String]("key")
    val value: Rep[String] = column[String]("value")
    val accessLevel: Rep[Int] = column[Int]("access_level_id")

  }
  /** Collection-like TableQuery object for table Form */
  lazy val table = new TableQuery(tag => new UI(tag))

}
