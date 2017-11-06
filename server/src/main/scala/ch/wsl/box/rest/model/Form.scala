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

  /** Entity class storing rows of table Form
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param name Database column name SqlType(text), Default(None)
    *  @param description Database column description SqlType(text), Default(None)
    *  @param layout Database column layout SqlType(text), Default(None) */
  case class Form_row(id: Option[Int] = None, name: String, table:String, description: Option[String] = None, layout: Option[String] = None, tableFields: Option[String] = None, query: Option[String] = None)
  /** GetResult implicit for fetching Form_row objects using plain SQL queries */

  /** Table description of table form. Objects of this class serve as prototypes for rows in queries. */
  class Form(_tableTag: Tag) extends profile.api.Table[Form_row](_tableTag, "form") {
    def * = (Rep.Some(id), name, table, description, layout, tableFields, query) <> (Form_row.tupled, Form_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), name, table, description, layout, tableFields, query).shaped.<>({r=>import r._; _1.map(_=> Form_row.tupled((_1, _2, _3, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text), Default(None) */
    val name: Rep[String] = column[String]("name")

    val table: Rep[String] = column[String]("table")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column layout SqlType(text), Default(None) */
    val layout: Rep[Option[String]] = column[Option[String]]("layout", O.Default(None))

    val tableFields: Rep[Option[String]] = column[Option[String]]("tableFields", O.Default(None))
    val query: Rep[Option[String]] = column[Option[String]]("query", O.Default(None))
  }
  /** Collection-like TableQuery object for table Form */
  lazy val table = new TableQuery(tag => new Form(tag))

}
