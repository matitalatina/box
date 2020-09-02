package ch.wsl.box.model.boxentities

import ch.wsl.box.jdbc.PostgresProfile.api._
import slick.model.ForeignKeyAction
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

/**
  * Created by andre on 5/15/2017.
  */
object BoxForm {



  val profile = ch.wsl.box.jdbc.PostgresProfile

  import profile._

  /** Entity class storing rows of table Form
    *  @param form_id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param name Database column name SqlType(text), Default(None)
    *  @param description Database column description SqlType(text), Default(None)
    *  @param layout Database column layout SqlType(text), Default(None) */
  case class BoxForm_row(form_id: Option[Int] = None, name: String, entity:String, description: Option[String] = None, layout: Option[String] = None,
                         tabularFields: Option[String] = None, query: Option[String] = None, exportFields: Option[String] = None)

  /** Table description of table form. Objects of this class serve as prototypes for rows in queries. */
  class BoxForm(_tableTag: Tag) extends profile.api.Table[BoxForm_row](_tableTag, "form") {
    def * = (Rep.Some(form_id), name, entity, description, layout, tabularFields, query,exportFields) <> (BoxForm_row.tupled, BoxForm_row.unapply)

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val form_id: Rep[Int] = column[Int]("form_id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text), Default(None) */
    val name: Rep[String] = column[String]("name")

    val entity: Rep[String] = column[String]("entity")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column layout SqlType(text), Default(None) */
    val layout: Rep[Option[String]] = column[Option[String]]("layout", O.Default(None))

    val tabularFields: Rep[Option[String]] = column[Option[String]]("tabularFields", O.Default(None))

    val exportFields: Rep[Option[String]] = column[Option[String]]("exportfields", O.Default(None))
    val query: Rep[Option[String]] = column[Option[String]]("query", O.Default(None))




  }
  /** Collection-like TableQuery object for table Form */
  lazy val BoxFormTable = new TableQuery(tag => new BoxForm(tag))


  /** Entity class storing rows of table Form_i18n
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param form_id Database column field_id SqlType(int4), Default(None)
    *  @param lang Database column lang SqlType(bpchar), Length(2,false), Default(None)
    *  @param label Database column title SqlType(text), Default(None)
    *  @param tooltip Database column tooltip SqlType(text), Default(None)
    *  @param hint Database column hint SqlType(text), Default(None)*/
  case class BoxForm_i18n_row(id: Option[Int] = None, form_id: Option[Int] = None,
                              lang: Option[String] = None, label: Option[String] = None,
                              tooltip: Option[String] = None, hint: Option[String] = None,
                              viewTable: Option[String] = None)
  /** GetResult implicit for fetching Form_i18n_row objects using plain SQL queries */

  /** Table description of table form_i18n. Objects of this class serve as prototypes for rows in queries. */
  class BoxForm_i18n(_tableTag: Tag) extends profile.api.Table[BoxForm_i18n_row](_tableTag, "form_i18n") {
    def * = (Rep.Some(id), form_id, lang, label, tooltip, hint,viewTable) <> (BoxForm_i18n_row.tupled, BoxForm_i18n_row.unapply)

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column field_id SqlType(int4), Default(None) */
    val form_id: Rep[Option[Int]] = column[Option[Int]]("form_id", O.Default(None))
    /** Database column lang SqlType(bpchar), Length(2,false), Default(None) */
    val lang: Rep[Option[String]] = column[Option[String]]("lang", O.Length(2,varying=false), O.Default(None))
    /** Database column title SqlType(text), Default(None) */
    val label: Rep[Option[String]] = column[Option[String]]("label", O.Default(None))
     /** Database column tooltip SqlType(text), Default(None) */
    val tooltip: Rep[Option[String]] = column[Option[String]]("tooltip", O.Default(None))
    /** Database column hint SqlType(text), Default(None) */
    val hint: Rep[Option[String]] = column[Option[String]]("hint", O.Default(None))

    val viewTable: Rep[Option[String]] = column[Option[String]]("view_table", O.Default(None))



    /** Foreign key referencing Field (database name fkey_field) */
    lazy val fieldFk = foreignKey("fkey_form", form_id, BoxFormTable)(r => Rep.Some(r.form_id), onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Form_i18n */
  lazy val BoxForm_i18nTable = new TableQuery(tag => new BoxForm_i18n(tag))


}
