package ch.wsl.box.rest

import ch.wsl.box.information_schema.{PgColumn, PgInformationSchema}
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

  def infoSchema(db:Database) = new PgInformationSchema("simple", db,db)

  "The service" should "query pgcolumn" in withDB { db =>

    val res: Future[Seq[PgColumn]] = db.run(infoSchema(db).pgColumns.result)

    res.map{ r =>
      println(r)
      r.nonEmpty shouldBe true
    }
  }

  it should "query pgConstraints" in withDB { db =>

    val res = db.run(infoSchema(db).pgConstraints.result)

    res.map{ r =>
      r.nonEmpty shouldBe true
    }
  }

  it should "query pgContraintsUsage" in withDB { db =>

    val res = db.run(infoSchema(db).pgContraintsUsage.result)

    res.map{ r =>
      r.nonEmpty shouldBe true
    }
  }

  it should "retrive pk" in withDB { db =>
      val res1  = db.run(infoSchema(db).pkQ.result)
      res1.map{r =>
        print(r)
        r.nonEmpty shouldBe true
      }

  }

}
