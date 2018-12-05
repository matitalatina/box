package ch.wsl.box.rest.boxentities

import java.sql.Timestamp

import ch.wsl.box.rest.jdbc.PostgresProfile.api._
import slick.model.ForeignKeyAction


object News {




  case class News_row(news_id: Option[Int] = None, datetime: java.time.LocalDateTime, author:Option[String]=None)
  /** GetResult implicit for fetching Form_row objects using plain SQL queries */

  /** Table description of table form. Objects of this class serve as prototypes for rows in queries. */
  class News(_tableTag: Tag) extends Table[News_row](_tableTag,"news") {
    def * = (Rep.Some(news_id), datetime, author) <> (News_row.tupled, News_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(news_id), datetime, author).shaped.<>({ r=>import r._; _1.map(_=> News_row.tupled((_1, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))


    val news_id: Rep[Int] = column[Int]("news_id", O.AutoInc, O.PrimaryKey)
    val datetime: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("datetime")

    val author: Rep[Option[String]] = column[Option[String]]("author")



  }
  /** Collection-like TableQuery object for table Form */
  lazy val News = new TableQuery(tag => new News(tag))


  /** Entity class storing rows of table Form_i18n
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param lang Database column lang SqlType(bpchar), Length(2,false), Default(None)
    *  @param label Database column title SqlType(text), Default(None)
    *  @param tooltip Database column tooltip SqlType(text), Default(None)
    *  @param hint Database column hint SqlType(text), Default(None)*/
  case class News_i18n_row(news_id: Option[Int] = None, lang: String,title:Option[String] = None, text: String)
  /** GetResult implicit for fetching Form_i18n_row objects using plain SQL queries */

  /** Table description of table form_i18n. Objects of this class serve as prototypes for rows in queries. */
  class News_i18n(_tableTag: Tag) extends Table[News_i18n_row](_tableTag, "news_i18n") {
    def * = (Rep.Some(news_id), lang, title, text) <> (News_i18n_row.tupled, News_i18n_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(news_id), lang, title, text).shaped.<>({ r=>import r._; _1.map(_=> News_i18n_row.tupled((_1, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val news_id: Rep[Int] = column[Int]("news_id", O.AutoInc, O.PrimaryKey)
    val lang: Rep[String] = column[String]("lang", O.Length(2,varying=false))
    val text: Rep[String] = column[String]("text")
    val title: Rep[Option[String]] = column[Option[String]]("title")


    /** Foreign key referencing Field (database name fkey_field) */
    lazy val fieldFk = foreignKey("news_i18n_news_id_fkey", news_id, News)(r => r.news_id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Form_i18n */
  lazy val News_i18n = new TableQuery(tag => new News_i18n(tag))
}
