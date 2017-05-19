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

  /** Entity class storing rows of table Field
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param form_id Database column form_id SqlType(int4)
    *  @param `type` Database column type SqlType(text)
    *  @param key Database column key SqlType(text), Default(None)
    *  @param widget Database column widget SqlType(text), Default(None)
    *  @param refModel Database column refModel SqlType(text), Default(None)
    *  @param refValueProperty Database column refValueProperty SqlType(text), Default(None)
    *  @param subform Database column subform SqlType(int4), Default(None) */
  case class Field_row(id: Option[Int] = None, form_id: Int, `type`: String, key: String, widget: Option[String] = None, refModel: Option[String] = None, refValueProperty: Option[String] = None, subform: Option[Int] = None)
  /** GetResult implicit for fetching Field_row objects using plain SQL queries */

  /** Table description of table field. Objects of this class serve as prototypes for rows in queries.
    *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Field(_tableTag: Tag) extends profile.api.Table[Field_row](_tableTag, "field") {
    def * = (Rep.Some(id), form_id, `type`, key, widget, refModel, refValueProperty, subform) <> (Field_row.tupled, Field_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(form_id), Rep.Some(`type`),  key, widget, refModel, refValueProperty, subform).shaped.<>({r=>import r._; _1.map(_=> Field_row.tupled((_1, _2.get, _3.get, _4, _5, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column form_id SqlType(int4) */
    val form_id: Rep[Int] = column[Int]("form_id")
    /** Database column type SqlType(text)
      *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type")
    /** Database column key SqlType(text), Default(None) */
    val key: Rep[String] = column[String]("key")
    /** Database column widget SqlType(text), Default(None) */
    val widget: Rep[Option[String]] = column[Option[String]]("widget", O.Default(None))
    /** Database column refModel SqlType(text), Default(None) */
    val refModel: Rep[Option[String]] = column[Option[String]]("refModel", O.Default(None))
    /** Database column refValueProperty SqlType(text), Default(None) */
    val refValueProperty: Rep[Option[String]] = column[Option[String]]("refValueProperty", O.Default(None))
    /** Database column subform SqlType(int4), Default(None) */
    val subform: Rep[Option[Int]] = column[Option[Int]]("subform", O.Default(None))

    /** Foreign key referencing Form (database name fkey_form) */
    lazy val formFk = foreignKey("fkey_form", form_id, Form.table)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Field */
  lazy val table = new TableQuery(tag => new Field(tag))

  /** Entity class storing rows of table Field_i18n
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param field_id Database column field_id SqlType(int4), Default(None)
    *  @param lang Database column lang SqlType(bpchar), Length(2,false), Default(None)
    *  @param title Database column title SqlType(text), Default(None)
    *  @param placeholder Database column placeholder SqlType(text), Default(None)
    *  @param tooltip Database column tooltip SqlType(text), Default(None)
    *  @param hint Database column hint SqlType(text), Default(None)
    *  @param refTextProperty Database column refTextProperty SqlType(text), Default(None) */
  case class Field_i18n_row(id: Option[Int] = None, field_id: Option[Int] = None, lang: Option[String] = None, title: Option[String] = None, placeholder: Option[String] = None, tooltip: Option[String] = None, hint: Option[String] = None, refTextProperty: Option[String] = None)
  /** GetResult implicit for fetching Field_i18n_row objects using plain SQL queries */

  /** Table description of table field_i18n. Objects of this class serve as prototypes for rows in queries. */
  class Field_i18n(_tableTag: Tag) extends profile.api.Table[Field_i18n_row](_tableTag, "field_i18n") {
    def * = (Rep.Some(id), field_id, lang, title, placeholder, tooltip, hint, refTextProperty) <> (Field_i18n_row.tupled, Field_i18n_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), field_id, lang, title, placeholder, tooltip, hint, refTextProperty).shaped.<>({r=>import r._; _1.map(_=> Field_i18n_row.tupled((_1, _2, _3, _4, _5, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column field_id SqlType(int4), Default(None) */
    val field_id: Rep[Option[Int]] = column[Option[Int]]("field_id", O.Default(None))
    /** Database column lang SqlType(bpchar), Length(2,false), Default(None) */
    val lang: Rep[Option[String]] = column[Option[String]]("lang", O.Length(2,varying=false), O.Default(None))
    /** Database column title SqlType(text), Default(None) */
    val title: Rep[Option[String]] = column[Option[String]]("title", O.Default(None))
    /** Database column placeholder SqlType(text), Default(None) */
    val placeholder: Rep[Option[String]] = column[Option[String]]("placeholder", O.Default(None))
    /** Database column tooltip SqlType(text), Default(None) */
    val tooltip: Rep[Option[String]] = column[Option[String]]("tooltip", O.Default(None))
    /** Database column hint SqlType(text), Default(None) */
    val hint: Rep[Option[String]] = column[Option[String]]("hint", O.Default(None))
    /** Database column refTextProperty SqlType(text), Default(None) */
    val refTextProperty: Rep[Option[String]] = column[Option[String]]("refTextProperty", O.Default(None))

    /** Foreign key referencing Field (database name fkey_field) */
    lazy val fieldFk = foreignKey("fkey_field", field_id, table)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Field_i18n */
  lazy val Field_i18n = new TableQuery(tag => new Field_i18n(tag))
}
