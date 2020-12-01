package ch.wsl.box.testmodel
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */


  import slick.model.ForeignKeyAction
  import slick.collection.heterogeneous._
  import slick.collection.heterogeneous.syntax._

object FileTables {


      import ch.wsl.box.jdbc.PostgresProfile.api._

      val profile = ch.wsl.box.jdbc.PostgresProfile

      import profile._

          import slick.model.ForeignKeyAction

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = profile.DDL(Nil, Nil)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema
}
