package ch.wsl.box.rest

import java.util.UUID

import ch.wsl.box.jdbc.FullDatabase
import ch.wsl.box.rest.logic.FormActions
import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes, JSONID, JSONKeyValue, JSONMetadata}
import ch.wsl.box.rest.metadata.FormMetadataFactory
import ch.wsl.box.testmodel.EntityActionsRegistry
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.model.boxentities.BoxField.{BoxFieldTable, BoxField_i18n_row, BoxField_row}
import ch.wsl.box.model.boxentities.BoxForm.{BoxFormTable, BoxForm_i18nTable, BoxForm_i18n_row, BoxForm_row}

import scala.concurrent.duration._

class UpdateFormSpec extends BaseSpec {

  val randomString = UUID.randomUUID().toString

  val jsonString3layer =
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

  val jsonString2layers =
    s"""
       |{
       |  "id": 1,
       |  "name": "parent",
       |  "child": [
       |     {
       |       "id": 1,
       |       "name": "child",
       |       "parent_id": 1
       |     }
       |  ]
       |}
    """.stripMargin.trim

  val jsonSingleLayer =
    s"""
       |{
       |  "id": 1,
       |  "name": "parent",
       |  "childs" : [
       |  ]
       |}
    """.stripMargin.trim

  import io.circe._, io.circe.parser._, io.circe.generic.auto._

  val json1Layer = parse(jsonSingleLayer) match {
    case Left(f) => {
      println(f.message)
      Json.Null
    }
    case Right(json) => json
  }

  val json2Layers = parse(jsonString2layers) match {
    case Left(f) => {
      println(f.message)
      Json.Null
    }
    case Right(json) => json
  }

  val json3Layers = parse(jsonString3layer) match {
    case Left(f) => {
      println(f.message)
      Json.Null
    }
    case Right(json) => json
  }

  val id = JSONID(Vector(JSONKeyValue("id","1")))


  val parentForm = BoxForm_row(
    name = "parent",
    entity = "parent",
    layout = Some(
      """
        |{
        |  "blocks" : [
        |    {
        |      "title" : null,
        |      "width" : 6,
        |      "fields" : [
        |       "id",
        |       "name",
        |       "childs"
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin)
  )

  val childForm = BoxForm_row(
    name = "child",
    entity = "child",
    layout = Some(
      """
        |{
        |  "blocks" : [
        |    {
        |      "title" : null,
        |      "width" : 6,
        |      "fields" : [
        |       "id",
        |       "name"
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin)
  )

  def parentFormFields(parentFormId:Int,childFormId:Int) = Seq(
    BoxField_row(form_id = parentFormId, `type` = JSONFieldTypes.NUMBER, name = "id"),
    BoxField_row(form_id = parentFormId, `type` = JSONFieldTypes.STRING, name = "name"),
    BoxField_row(form_id = parentFormId, `type` = JSONFieldTypes.CHILD, name = "childs",child_form_id = Some(childFormId),masterFields = Some("id"),childFields = Some("parent_id"))
  )

  def childFormFields(childFormId:Int) = Seq(
    BoxField_row(form_id = childFormId, `type` = JSONFieldTypes.NUMBER, name = "id"),
    BoxField_row(form_id = childFormId, `type` = JSONFieldTypes.STRING, name = "name"),
  )


  "The service" should "query update nested subforms" in withUserProfile { implicit up =>

    implicit val bdb = FullDatabase(up.db,up.db)

    val insertForm = for{
      parentId <- up.boxDb.run(BoxFormTable.returning(BoxFormTable.map(_.form_id)) += parentForm)
      childId <- up.boxDb.run(BoxFormTable.returning(BoxFormTable.map(_.form_id)) += childForm)
      _ <- up.boxDb.run(DBIO.sequence(parentFormFields(parentId,childId).map(x => BoxFieldTable += x)))
      _ <- up.boxDb.run(DBIO.sequence(childFormFields(childId).map(x => BoxFieldTable += x)))
    } yield true



    val future = for{
      _ <- insertForm
      form <- FormMetadataFactory(up.boxDb,up.db).of("parent","it")
      actions = FormActions(form,EntityActionsRegistry.apply,FormMetadataFactory(up.boxDb,up.db))
      i <- up.db.run(actions.upsertIfNeeded(Some(id),json1Layer).transactionally)
      result <- up.db.run(actions.getById(id))
    } yield result




    future.recover{ case t:Throwable =>
      t.printStackTrace()
    }

    future.map{ result =>
      println(result)
      result.get.equals(json1Layer) shouldBe true
    }

  }

}
