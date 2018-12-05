package ch.wsl.box.rest.boxentities

//import ch.wsl.box.model.FileTables.{Document, profile}
import ch.wsl.box.rest.jdbc.PostgresProfile.api._

/**
  * Created by andre on 5/15/2017.
  */
object UIscrTable {

  val profile = ch.wsl.box.rest.jdbc.PostgresProfile
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plai
  // n SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

//  /** DDL for all tables. Call .create to execute. */
//  lazy val schema: profile.SchemaDescription =               Document.schema
//  @deprecated("Use .schema instead of .ddl", "3.0")
//  def ddl = schema

  case class UIsrc_row(id: Option[Int] = None, file: Option[Array[Byte]], mime:Option[String], name:Option[String], accessLevel:Int)

  class UIsrc(_tableTag: Tag) extends profile.api.Table[UIsrc_row](_tableTag, "ui_src") {
    def * = (Rep.Some(id), file, mime, name, accessLevel) <> (UIsrc_row.tupled, UIsrc_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id),  file, mime, name, accessLevel).shaped.<>({r=>import r._; _1.map(_=> UIsrc_row.tupled((_1, _2, _3, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val file: Rep[Option[Array[Byte]]] = column[Option[Array[Byte]]]("file")
    val mime: Rep[Option[String]] = column[Option[String]]("mime")
    val name: Rep[Option[String]] = column[Option[String]]("name")
    val accessLevel: Rep[Int] = column[Int]("access_level_id")

  }
  /** Collection-like TableQuery object for table Form */
  lazy val table = new TableQuery(tag => new UIsrc(tag))

}
