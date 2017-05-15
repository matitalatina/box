package ch.wsl.box.rest.model

import slick.driver.PostgresDriver.api._
import slick.model.ForeignKeyAction
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

/**
  * Created by andre on 5/15/2017.
  */
object Form {



  val profile = slick.driver.PostgresDriver

  import profile._

  /** Entity class storing rows of table form
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param name Database column name SqlType(text), Default(None)
    *  @param description Database column description SqlType(text), Default(None) */
  case class form_row(id: Option[Int] = None, name: String, description: Option[String] = None)
  /** GetResult implicit for fetching form_row objects using plain SQL queries */

  /** Table description of table form. Objects of this class serve as prototypes for rows in queries. */
  class form(_tableTag: Tag) extends profile.api.Table[form_row](_tableTag, Some("box"), "form") {
    def * = (Rep.Some(id), name, description) <> (form_row.tupled, form_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), name, description).shaped.<>({r=>import r._; _1.map(_=> form_row.tupled((_1, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
  }
  /** Collection-like TableQuery object for table form */
  lazy val table = new TableQuery(tag => new form(tag))

}
