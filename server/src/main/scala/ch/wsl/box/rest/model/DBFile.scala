package ch.wsl.box.rest.model

object DBFile {

  val profile = slick.jdbc.PostgresProfile

  import profile._
  import api._

  /** Entity class storing rows of table File
    *  @param file_id Database column file_id SqlType(serial), AutoInc, PrimaryKey
    *  @param file Database column file SqlType(bytea)
    *  @param name Database column name SqlType(text)
    *  @param mime Database column mime SqlType(text) */
  case class Row(file_id: Option[Int] = None, file: Array[Byte], name: String, mime: String,insert_by:String)
  /** GetResult implicit for fetching File_row objects using plain SQL queries */

  /** Table description of table file. Objects of this class serve as prototypes for rows in queries. */
  class Table(_tableTag: Tag) extends profile.api.Table[Row](_tableTag, "file") {
    def * = (Rep.Some(file_id), file, name, mime,insert_by) <> (Row.tupled, Row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(file_id), Rep.Some(file), Rep.Some(name), Rep.Some(mime), Rep.Some(insert_by)).shaped.<>({r=>import r._; _1.map(_=> Row.tupled((_1, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column file_id SqlType(serial), AutoInc, PrimaryKey */
    val file_id: Rep[Int] = column[Int]("file_id", O.AutoInc, O.PrimaryKey)
    /** Database column file SqlType(bytea) */
    val file: Rep[Array[Byte]] = column[Array[Byte]]("file")
    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column mime SqlType(text) */
    val mime: Rep[String] = column[String]("mime")
    /** Database column insert_by SqlType(text) */
    val insert_by: Rep[String] = column[String]("insert_by")
  }
  /** Collection-like TableQuery object for table File */
  lazy val table = new TableQuery(tag => new Table(tag))
}
