package ch.wsl.box.rest.jdbc


import com.github.tminglei.slickpg._
import slick.basic.Capability
import slick.driver.JdbcProfile

trait PostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
//  with PgRangeSupport
//  with PgHStoreSupport
//  with PgPlayJsonSupport
  with PgSearchSupport
//  with PgPostGISSupport
//  with PgNetSupport
//  with PgLTreeSupport
{
  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcProfile.capabilities.insertOrUpdate

  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits
    with DateTimeImplicits
//    with JsonImplicits
//    with NetImplicits
//    with LTreeImplicits
//    with RangeImplicits
//    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants
  {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val intListTypeMapper = new SimpleArrayJdbcType[Int]("int4").to(_.toList)
    //    implicit val dblListTypeMapper = new SimpleArrayJdbcType[Double]("double precision").to(_.toList)
    implicit val dblListTypeMapper = new SimpleArrayJdbcType[Double]("float8").to(_.toList)
  }
}

object PostgresProfile extends PostgresProfile
