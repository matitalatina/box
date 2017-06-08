package ch.wsl.box.rest.model


import slick.driver.PostgresDriver.api._
import slick.model.ForeignKeyAction
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

/**
  * Created by andre on 5/15/2017.
  */
object Labels {



  val profile = slick.driver.PostgresDriver

  import profile._


  case class Labels_row(id: Option[Int] = None, lang: String, key:String, label: Option[String] = None)

  class Labels(_tableTag: Tag) extends profile.api.Table[Labels_row](_tableTag, "labels") {
    def * = (Rep.Some(id), lang, key, label) <> (Labels_row.tupled, Labels_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), lang, key, label).shaped.<>({r=>import r._; _1.map(_=> Labels_row.tupled((_1, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val lang: Rep[String] = column[String]("lang")
    val key: Rep[String] = column[String]("key")
    val label: Rep[Option[String]] = column[Option[String]]("label", O.Default(None))

  }
  /** Collection-like TableQuery object for table Form */
  lazy val table = new TableQuery(tag => new Labels(tag))

}
