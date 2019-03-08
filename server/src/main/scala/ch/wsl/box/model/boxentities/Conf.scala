package ch.wsl.box.model.boxentities

import ch.wsl.box.rest.utils.Auth
import ch.wsl.box.rest.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */
object Conf {

  val profile = ch.wsl.box.rest.jdbc.PostgresProfile


  case class Conf_row(id: Option[Int] = None, key:String, value: Option[String] = None)

  class Conf(_tableTag: Tag) extends profile.api.Table[Conf_row](_tableTag, "conf") {
    def * = (Rep.Some(id), key, value) <> (Conf_row.tupled, Conf_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id),  key, value).shaped.<>({r=>import r._; _1.map(_=> Conf_row.tupled((_1, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val key: Rep[String] = column[String]("key")
    val value: Rep[Option[String]] = column[Option[String]]("value", O.Default(None))

  }
  /** Collection-like TableQuery object for table Conf  */
  lazy val table = new TableQuery(tag => new Conf(tag))

}
