package ch.wsl.box.model.boxentities


import ch.wsl.box.jdbc.PostgresProfile.api._
import slick.model.ForeignKeyAction
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

/**
  * Created by andre on 5/15/2017.
  */
object BoxLabels {



  val profile = ch.wsl.box.jdbc.PostgresProfile

  import profile._


  case class BoxLabels_row(id: Option[Int] = None, lang: String, key:String, label: Option[String] = None)

  class BoxLabels(_tableTag: Tag) extends profile.api.Table[BoxLabels_row](_tableTag, "labels") {
    def * = (Rep.Some(id), lang, key, label) <> (BoxLabels_row.tupled, BoxLabels_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), lang, key, label).shaped.<>({r=>import r._; _1.map(_=> BoxLabels_row.tupled((_1, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val lang: Rep[String] = column[String]("lang")
    val key: Rep[String] = column[String]("key")
    val label: Rep[Option[String]] = column[Option[String]]("label", O.Default(None))

  }
  /** Collection-like TableQuery object for table Form */
  lazy val BoxLabelsTable = new TableQuery(tag => new BoxLabels(tag))

}
