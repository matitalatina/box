package ch.wsl.box.model.boxentities




/**
  * Created by andre on 5/15/2017.
  */
object ExportField {


  val profile = ch.wsl.box.rest.jdbc.PostgresProfile

  import profile._
  import api._


  case class ExportField_row(field_id: Option[Int] = None, export_id: Int, `type`: String, name: String, widget: Option[String] = None,
                       lookupEntity: Option[String] = None, lookupValueField: Option[String] = None, lookupQuery:Option[String] = None,
                       default:Option[String] = None, conditionFieldId:Option[String] = None, conditionValues:Option[String] = None)
  /** GetResult implicit for fetching Field_row objects using plain SQL queries */

  /** Table description of table field. Objects of this class serve as prototypes for rows in queries.
    *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class ExportField(_tableTag: Tag) extends profile.api.Table[ExportField_row](_tableTag, "export_field") {
    def * = (Rep.Some(field_id), export_id, `type`, name, widget, lookupEntity, lookupValueField, lookupQuery, default,conditionFieldId,conditionValues) <> (ExportField_row.tupled, ExportField_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(field_id), Rep.Some(export_id), Rep.Some(`type`),  name, widget, lookupEntity, lookupValueField, lookupQuery, default,conditionFieldId,conditionValues).shaped.<>({ r=>import r._; _1.map(_=> ExportField_row.tupled((_1, _2.get, _3.get, _4, _5, _6, _7, _8, _9, _10, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val field_id: Rep[Int] = column[Int]("field_id", O.AutoInc, O.PrimaryKey)
    /** Database column form_id SqlType(int4) */
    val export_id: Rep[Int] = column[Int]("export_id")
    /** Database column type SqlType(text)
      *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type")
    /** Database column key SqlType(text), Default(None) */
    val name: Rep[String] = column[String]("name")
    /** Database column widget SqlType(text), Default(None) */
    val widget: Rep[Option[String]] = column[Option[String]]("widget", O.Default(None))
    /** Database column refModel SqlType(text), Default(None) */
    val lookupEntity: Rep[Option[String]] = column[Option[String]]("lookupEntity", O.Default(None))
    /** Database column refValueProperty SqlType(text), Default(None) */
    val lookupValueField: Rep[Option[String]] = column[Option[String]]("lookupValueField", O.Default(None))
    val lookupQuery: Rep[Option[String]] = column[Option[String]]("lookupQuery", O.Default(None))
    /** Database column subform SqlType(int4), Default(None) */
    val default: Rep[Option[String]] = column[Option[String]]("default", O.Default(None))
    val conditionFieldId: Rep[Option[String]] = column[Option[String]]("conditionFieldId", O.Default(None))
    val conditionValues: Rep[Option[String]] = column[Option[String]]("conditionValues", O.Default(None))

    /** Foreign key referencing Form (database name fkey_form) */
    lazy val exportFk = foreignKey("fkey_export", export_id, Export.Export)(r => r.export_id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Field */
  lazy val ExportField = new TableQuery(tag => new ExportField(tag))

  /** Entity class storing rows of table Field_i18n
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param field_id Database column field_id SqlType(int4), Default(None)
    *  @param lang Database column lang SqlType(bpchar), Length(2,false), Default(None)
    *  @param label Database column title SqlType(text), Default(None)
    *  @param placeholder Database column placeholder SqlType(text), Default(None)
    *  @param tooltip Database column tooltip SqlType(text), Default(None)
    *  @param hint Database column hint SqlType(text), Default(None)
    *  @param lookupTextField Database column refTextProperty SqlType(text), Default(None) */
  case class ExportField_i18n_row(id: Option[Int] = None, field_id: Option[Int] = None, lang: Option[String] = None, label: Option[String] = None,
                            placeholder: Option[String] = None, tooltip: Option[String] = None, hint: Option[String] = None,
                            lookupTextField: Option[String] = None)
  /** GetResult implicit for fetching Field_i18n_row objects using plain SQL queries */

  /** Table description of table field_i18n. Objects of this class serve as prototypes for rows in queries. */
  class ExportField_i18n(_tableTag: Tag) extends profile.api.Table[ExportField_i18n_row](_tableTag, "export_field_i18n") {
    def * = (Rep.Some(id), field_id, lang, label, placeholder, tooltip, hint, lookupTextField) <> (ExportField_i18n_row.tupled, ExportField_i18n_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), field_id, lang, label, placeholder, tooltip, hint, lookupTextField).shaped.<>({ r=>import r._; _1.map(_=> ExportField_i18n_row.tupled((_1, _2, _3, _4, _5, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column field_id SqlType(int4), Default(None) */
    val field_id: Rep[Option[Int]] = column[Option[Int]]("field_id", O.Default(None))
    /** Database column lang SqlType(bpchar), Length(2,false), Default(None) */
    val lang: Rep[Option[String]] = column[Option[String]]("lang", O.Length(2,varying=false), O.Default(None))
    /** Database column title SqlType(text), Default(None) */
    val label: Rep[Option[String]] = column[Option[String]]("label", O.Default(None))
    /** Database column placeholder SqlType(text), Default(None) */
    val placeholder: Rep[Option[String]] = column[Option[String]]("placeholder", O.Default(None))
    /** Database column tooltip SqlType(text), Default(None) */
    val tooltip: Rep[Option[String]] = column[Option[String]]("tooltip", O.Default(None))
    /** Database column hint SqlType(text), Default(None) */
    val hint: Rep[Option[String]] = column[Option[String]]("hint", O.Default(None))
    /** Database column refTextProperty SqlType(text), Default(None) */
    val lookupTextField: Rep[Option[String]] = column[Option[String]]("lookupTextField", O.Default(None))

    /** Foreign key referencing Field (database name fkey_field) */
    lazy val fieldFk = foreignKey("fkey_field", field_id, ExportField)(r => Rep.Some(r.field_id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Field_i18n */
  lazy val ExportField_i18n = new TableQuery(tag => new ExportField_i18n(tag))


  case class ExportHeader_i18n_row(id: Option[Int] = None, key:String, lang:String, label:String)

  class ExportHeader_i18n(_tableTag: Tag) extends profile.api.Table[ExportHeader_i18n_row](_tableTag, "export_header_i18n") {
    def * = (Rep.Some(id), key, lang, label) <> (ExportHeader_i18n_row.tupled, ExportHeader_i18n_row.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val key: Rep[String] = column[String]("key")
    val lang: Rep[String] = column[String]("lang")
    val label: Rep[String] = column[String]("label")


  }
  /** Collection-like TableQuery object for table Field_i18n */
  lazy val ExportHeader_i18n = new TableQuery(tag => new ExportHeader_i18n(tag))


}

