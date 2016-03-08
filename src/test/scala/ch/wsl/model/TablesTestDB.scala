package ch.wsl.model
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */

import slick.driver.PostgresDriver.api._
import slick.model.ForeignKeyAction
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

/**
  * Copy-pasted from the generated version
  */
package object tablesTestDB {


  val profile = slick.driver.PostgresDriver

  import profile._

  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = A.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table A
    *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
    *  @param string1 Database column string1 SqlType(text), Default(None)
    *  @param string2 Database column string2 SqlType(varchar), Length(255,true), Default(None)
    *  @param short Database column short SqlType(int2), Default(None)
    *  @param integer Database column integer SqlType(int4), Default(None)
    *  @param double Database column double SqlType(float8), Default(None)
    *  @param double2 Database column double2 SqlType(float4), Default(None)
    *  @param long Database column long SqlType(int8), Default(None) */
  case class ARow(id: Int, string1: Option[String] = None, string2: Option[String] = None, short: Option[Short] = None, integer: Option[Int] = None, double: Option[Double] = None, double2: Option[Float] = None, long: Option[Long] = None)
  /** GetResult implicit for fetching ARow objects using plain SQL queries */

  /** Table description of table a. Objects of this class serve as prototypes for rows in queries. */
  class A(_tableTag: Tag) extends Table[ARow](_tableTag, "a") {
    def * = (id, string1, string2, short, integer, double, double2, long) <> (ARow.tupled, ARow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), string1, string2, short, integer, double, double2, long).shaped.<>({r=>import r._; _1.map(_=> ARow.tupled((_1.get, _2, _3, _4, _5, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column string1 SqlType(text), Default(None) */
    val string1: Rep[Option[String]] = column[Option[String]]("string1", O.Default(None))
    /** Database column string2 SqlType(varchar), Length(255,true), Default(None) */
    val string2: Rep[Option[String]] = column[Option[String]]("string2", O.Length(255,varying=true), O.Default(None))
    /** Database column short SqlType(int2), Default(None) */
    val short: Rep[Option[Short]] = column[Option[Short]]("short", O.Default(None))
    /** Database column integer SqlType(int4), Default(None) */
    val integer: Rep[Option[Int]] = column[Option[Int]]("integer", O.Default(None))
    /** Database column double SqlType(float8), Default(None) */
    val double: Rep[Option[Double]] = column[Option[Double]]("double", O.Default(None))
    /** Database column double2 SqlType(float4), Default(None) */
    val double2: Rep[Option[Float]] = column[Option[Float]]("double2", O.Default(None))
    /** Database column long SqlType(int8), Default(None) */
    val long: Rep[Option[Long]] = column[Option[Long]]("long", O.Default(None))
  }
  /** Collection-like TableQuery object for table A */
  lazy val A = new TableQuery(tag => new A(tag))
}
