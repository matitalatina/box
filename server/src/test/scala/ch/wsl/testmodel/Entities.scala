package ch.wsl.box.testmodel
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */


  import slick.model.ForeignKeyAction
  import slick.collection.heterogeneous._
  import slick.collection.heterogeneous.syntax._

object Entities {


      import ch.wsl.box.jdbc.PostgresProfile.api._

      val profile = ch.wsl.box.jdbc.PostgresProfile

      import profile._

          import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Child.schema ++ Parent.schema ++ Simple.schema ++ Subchild.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Child
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(text), Default(None)
   *  @param parent_id Database column parent_id SqlType(int4), Default(None) */
  case class Child_row(id: Option[Int] = None, name: Option[String] = None, parent_id: Option[Int] = None)
  /** GetResult implicit for fetching Child_row objects using plain SQL queries */

  /** Table description of table child. Objects of this class serve as prototypes for rows in queries. */
  class Child(_tableTag: Tag) extends profile.api.Table[Child_row](_tableTag, "child") {
    def * = (Rep.Some(id), name, parent_id) <> (Child_row.tupled, Child_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), name, parent_id).shaped.<>({r=>import r._; _1.map(_=> Child_row.tupled((_1, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))
    /** Database column parent_id SqlType(int4), Default(None) */
    val parent_id: Rep[Option[Int]] = column[Option[Int]]("parent_id", O.Default(None))

    /** Foreign key referencing Parent (database name child_parent_id_fk) */
    lazy val parentFk = foreignKey("child_parent_id_fk", parent_id, Parent)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Child */
  lazy val Child = new TableQuery(tag => new Child(tag))

  /** Entity class storing rows of table Parent
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(text), Default(None) */
  case class Parent_row(id: Option[Int] = None, name: Option[String] = None)
  /** GetResult implicit for fetching Parent_row objects using plain SQL queries */

  /** Table description of table parent. Objects of this class serve as prototypes for rows in queries. */
  class Parent(_tableTag: Tag) extends profile.api.Table[Parent_row](_tableTag, "parent") {
    def * = (Rep.Some(id), name) <> (Parent_row.tupled, Parent_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), name).shaped.<>({r=>import r._; _1.map(_=> Parent_row.tupled((_1, _2)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))
  }
  /** Collection-like TableQuery object for table Parent */
  lazy val Parent = new TableQuery(tag => new Parent(tag))

  /** Entity class storing rows of table Simple
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(text), Default(None) */
  case class Simple_row(id: Option[Int] = None, name: Option[String] = None)
  /** GetResult implicit for fetching Simple_row objects using plain SQL queries */

  /** Table description of table simple. Objects of this class serve as prototypes for rows in queries. */
  class Simple(_tableTag: Tag) extends profile.api.Table[Simple_row](_tableTag, "simple") {
    def * = (Rep.Some(id), name) <> (Simple_row.tupled, Simple_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), name).shaped.<>({r=>import r._; _1.map(_=> Simple_row.tupled((_1, _2)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(text), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))
  }
  /** Collection-like TableQuery object for table Simple */
  lazy val Simple = new TableQuery(tag => new Simple(tag))

  /** Entity class storing rows of table Subchild
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param child_id Database column child_id SqlType(int4), Default(None)
   *  @param name Database column name SqlType(text), Default(None) */
  case class Subchild_row(id: Option[Int] = None, child_id: Option[Int] = None, name: Option[String] = None)
  /** GetResult implicit for fetching Subchild_row objects using plain SQL queries */

  /** Table description of table subchild. Objects of this class serve as prototypes for rows in queries. */
  class Subchild(_tableTag: Tag) extends profile.api.Table[Subchild_row](_tableTag, "subchild") {
    def * = (Rep.Some(id), child_id, name) <> (Subchild_row.tupled, Subchild_row.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), child_id, name).shaped.<>({r=>import r._; _1.map(_=> Subchild_row.tupled((_1, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column child_id SqlType(int4), Default(None) */
    val child_id: Rep[Option[Int]] = column[Option[Int]]("child_id", O.Default(None))
    /** Database column name SqlType(text), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))

    /** Foreign key referencing Child (database name subchild_child_id_fk) */
    lazy val childFk = foreignKey("subchild_child_id_fk", child_id, Child)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Subchild */
  lazy val Subchild = new TableQuery(tag => new Subchild(tag))
}
