package ch.wsl.box.model.boxentities

import ch.wsl.box.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */
object BoxPublicEntities {

  val profile = ch.wsl.box.jdbc.PostgresProfile


  case class Row(id: Option[Int] = None, entity:String, update: Boolean, insert: Boolean)

  class Table(_tableTag: Tag) extends profile.api.Table[Row](_tableTag, "public_entities") {
    def * = (Rep.Some(id), entity, update, insert) <> (Row.tupled, Row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id),  entity, update, insert).shaped.<>({r=>import r._; _1.map(_=> Row.tupled((_1, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val entity: Rep[String] = column[String]("entity")
    val insert: Rep[Boolean] = column[Boolean]("insert")
    val update: Rep[Boolean] = column[Boolean]("update")

  }
  /** Collection-like TableQuery object for table Conf  */
  lazy val table = new TableQuery(tag => new Table(tag))

}
