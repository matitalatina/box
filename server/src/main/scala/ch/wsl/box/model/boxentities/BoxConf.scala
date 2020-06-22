package ch.wsl.box.model.boxentities

import ch.wsl.box.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */
object BoxConf {

  val profile = ch.wsl.box.jdbc.PostgresProfile


  case class BoxConf_row(id: Option[Int] = None, key:String, value: Option[String] = None)

  class BoxConf(_tableTag: Tag) extends profile.api.Table[BoxConf_row](_tableTag, "conf") {
    def * = (Rep.Some(id), key, value) <> (BoxConf_row.tupled, BoxConf_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id),  key, value).shaped.<>({r=>import r._; _1.map(_=> BoxConf_row.tupled((_1, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val key: Rep[String] = column[String]("key")
    val value: Rep[Option[String]] = column[Option[String]]("value", O.Default(None))

  }
  /** Collection-like TableQuery object for table Conf  */
  lazy val BoxConfTable = new TableQuery(tag => new BoxConf(tag))

}
