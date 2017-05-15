package ch.wsl.box.rest.model


import slick.driver.PostgresDriver.api._
import slick.model.ForeignKeyAction
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

/**
  * Created by andre on 5/15/2017.
  */
object Field {


  val profile = slick.driver.PostgresDriver

  import profile._

  /** Entity class storing rows of table field
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param form_id Database column form_id SqlType(int4)
    *  @param `type` Database column type SqlType(text)
    *  @param table Database column table SqlType(text)
    *  @param key Database column key SqlType(text), Default(None)
    *  @param title Database column title SqlType(text), Default(None)
    *  @param placeholder Database column placeholder SqlType(text), Default(None)
    *  @param widget Database column widget SqlType(text), Default(None)
    *  @param refModel Database column refModel SqlType(text), Default(None)
    *  @param refValueProperty Database column refValueProperty SqlType(text), Default(None)
    *  @param refTextProperty Database column refTextProperty SqlType(text), Default(None) */
  case class field_row(id: Option[Int] = None, form_id: Int, `type`: String, table: String, key: Option[String] = None, title: Option[String] = None, placeholder: Option[String] = None, widget: Option[String] = None, refModel: Option[String] = None, refValueProperty: Option[String] = None, refTextProperty: Option[String] = None)
  /** GetResult implicit for fetching field_row objects using plain SQL queries */

  /** Table description of table field. Objects of this class serve as prototypes for rows in queries.
    *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class field(_tableTag: Tag) extends profile.api.Table[field_row](_tableTag, Some("box"), "field") {
    def * = (Rep.Some(id), form_id, `type`, table, key, title, placeholder, widget, refModel, refValueProperty, refTextProperty) <> (field_row.tupled, field_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(form_id), Rep.Some(`type`), Rep.Some(table), key, title, placeholder, widget, refModel, refValueProperty, refTextProperty).shaped.<>({r=>import r._; _1.map(_=> field_row.tupled((_1, _2.get, _3.get, _4.get, _5, _6, _7, _8, _9, _10, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column form_id SqlType(int4) */
    val form_id: Rep[Int] = column[Int]("form_id")
    /** Database column type SqlType(text)
      *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type")
    /** Database column table SqlType(text) */
    val table: Rep[String] = column[String]("table")
    /** Database column key SqlType(text), Default(None) */
    val key: Rep[Option[String]] = column[Option[String]]("key", O.Default(None))
    /** Database column title SqlType(text), Default(None) */
    val title: Rep[Option[String]] = column[Option[String]]("title", O.Default(None))
    /** Database column placeholder SqlType(text), Default(None) */
    val placeholder: Rep[Option[String]] = column[Option[String]]("placeholder", O.Default(None))
    /** Database column widget SqlType(text), Default(None) */
    val widget: Rep[Option[String]] = column[Option[String]]("widget", O.Default(None))
    /** Database column refModel SqlType(text), Default(None) */
    val refModel: Rep[Option[String]] = column[Option[String]]("refModel", O.Default(None))
    /** Database column refValueProperty SqlType(text), Default(None) */
    val refValueProperty: Rep[Option[String]] = column[Option[String]]("refValueProperty", O.Default(None))
    /** Database column refTextProperty SqlType(text), Default(None) */
    val refTextProperty: Rep[Option[String]] = column[Option[String]]("refTextProperty", O.Default(None))

    /** Foreign key referencing form (database name fkey_form) */
    lazy val formFk = foreignKey("fkey_form", form_id, Form.table)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table field */
  lazy val table = new TableQuery(tag => new field(tag))
}
