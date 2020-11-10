package ch.wsl.box.rest

import java.util.UUID

import ch.wsl.box.rest.logic.FormActions

import ch.wsl.box.model.shared.{JSONID, JSONKeyValue}
import ch.wsl.box.rest.metadata.FormMetadataFactory
import ch.wsl.box.testmodel.EntityActionsRegistry
import ch.wsl.box.jdbc.PostgresProfile.api._

import scala.concurrent.duration._

class UpdateFormSpec extends BaseSpec {

  val randomString = UUID.randomUUID().toString

  val jsonString =
    s"""
      |{
      |  "id": 1,
      |  "name": "parent",
      |  "child": [
      |     {
      |       "id": 1,
      |       "name": "child",
      |       "parent_id": 1,
      |       "subchild": [
      |         {
      |           "id": 1,
      |           "name": "$randomString",
      |           "child_id": 1
      |         }
      |       ]
      |     }
      |  ]
      |}
    """.stripMargin.trim

  import io.circe._, io.circe.parser._, io.circe.generic.auto._

  val json = parse(jsonString) match {
    case Left(f) => {
      println(f.message)
      Json.Null
    }
    case Right(json) => json
  }
  val id = JSONID(Vector(JSONKeyValue("id","1")))





  "The service" should "query update nested subforms" in withUserProfile { implicit up =>


    val future = for{
      form <- FormMetadataFactory().of("parent","it")
      actions = FormActions(form,EntityActionsRegistry.apply,FormMetadataFactory())(up.db,materializer,ec)
      i <- up.db.run(actions.updateIfNeeded(id,json)(up.db).transactionally)
      result <- up.db.run(actions.getById(id)(up.db))
    } yield result




    future.recover{ case t:Throwable =>
      t.printStackTrace()
    }

    future.map{ result =>
      assert(result.get == json)
    }

  }

}
