package ch.wsl.box.rest

import ch.wsl.box.rest.logic.{PgColumn, PgColumns, PgInformationSchema}
import org.scalatest.concurrent.ScalaFutures
import slick.lifted.TableQuery
import ch.wsl.box.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.reflect.macros.whitebox
import scala.concurrent.duration._

/**
  * Created by pezzatti on 7/20/17.
  *
  * server/test-only ch.wsl.box.rest.InformationSchemaSpec
  */
class InformationSchemaSpec extends BaseSpec {

  val infoSchema = new PgInformationSchema("simple", db)

  "The service" should "query pgcolumn" in {

    val res: Future[Seq[PgColumn]] = db.run(infoSchema.pgColumns.result)

    whenReady(res, timeout(10 seconds)) { r =>
      assert(r.size > 0)
    }
  }

  it should "query pgConstraints" in {

    val res = db.run(infoSchema.pgConstraints.result)

    whenReady(res, timeout(10 seconds)) { r =>
      assert(r.size > 0)
    }
  }

  it should "query pgContraintsUsage" in {

    val res = db.run(infoSchema.pgContraintsUsage.result)

    whenReady(res, timeout(10 seconds)) { r =>
      assert(r.size > 0)
    }
  }

  it should "retrive pk" in {
      val res1  = db.run(infoSchema.pkQ.result)
      whenReady(res1, timeout(10 seconds)){r =>
        print(r)
        assert(r.size >0)
      }

  }

}
