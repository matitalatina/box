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
import ch.wsl.box.rest.utils.UserProfile
import io.circe.Json

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import ch.wsl.box.shared.utils.JSONUtils._


class UpdateFormSpec extends BaseSpec {




  val appManagedLayers = AppManagedIdFixtures.layers.mapValues(stringToJson)
  val dbManagedLayers = DbManagedIdFixtures.layers.mapValues(stringToJson)


  val id = JSONID(Vector(JSONKeyValue("id","1")))



  def upsert(formName:String,id:Option[JSONID],json:Json)(implicit up:UserProfile, fdb: FullDatabase) = {
    for{
      form <- FormMetadataFactory(up.boxDb,up.db).of(formName,"it")
      actions = FormActions(form,EntityActionsRegistry.apply,FormMetadataFactory(up.boxDb,up.db))
      i <- up.db.run(actions.upsertIfNeeded(id,json).transactionally)
      result <- up.db.run(actions.getById(i))
    } yield result

  }

  def appManagedUpsert(id:JSONID, json:Json)(implicit up:UserProfile, fdb: FullDatabase) = {
    for{
      _ <- new FormFixtures("app_").insertForm()
      result <- upsert("app_parent",Some(id),json)
    } yield result.get shouldBe json
  }

  def dbManagedUpsert(json:Json)(assertion:Json => org.scalatest.Assertion)(implicit up:UserProfile, fdb: FullDatabase) = {
    for{
      _ <- new FormFixtures("db_").insertForm()
      result <- upsert("db_parent",None,json)
    } yield assertion(result.get)
  }




  "App managed form"  should "insert a single layer json"  in withUserProfile { implicit up =>
    implicit val bdb = FullDatabase(up.db,up.db)

    appManagedUpsert(id,appManagedLayers(1))

  }

  it should "insert a 2 layer json" in withUserProfile { implicit up =>

    implicit val bdb = FullDatabase(up.db,up.db)

    appManagedUpsert(id,appManagedLayers(2))

  }

  it should "insert a 3 layer json" in withUserProfile { implicit up =>

    implicit val bdb = FullDatabase(up.db,up.db)

    appManagedUpsert(id,appManagedLayers(3))

  }

  "Db managed form" should "insert a single layer json" in withUserProfile { implicit up =>
    implicit val bdb = FullDatabase(up.db,up.db)

    dbManagedUpsert(dbManagedLayers(1)){ json =>
      json.get("name") shouldBe "parent"
    }

  }

  it should "insert a 2 layer json" in withUserProfile { implicit up =>

    implicit val bdb = FullDatabase(up.db,up.db)

    dbManagedUpsert(dbManagedLayers(2)) { json =>
      json.get("name") shouldBe "parent"
      val childs = json.seq("childs")
      childs.length shouldBe 1
      childs.head.get("name") shouldBe "child"
    }

  }

  it should "insert a 3 layer json" in withUserProfile { implicit up =>

    implicit val bdb = FullDatabase(up.db,up.db)

    dbManagedUpsert(dbManagedLayers(3)) { json =>
      json.get("name") shouldBe "parent"
      val childs = json.seq("childs")
      childs.length shouldBe 1
      childs.head.get("name") shouldBe "child"
      val subchilds = json.seq("subchilds")
      subchilds.length shouldBe 1
      subchilds.head.get("name") shouldBe "subchild"
    }

  }


}

class FormFixtures(tablePrefix:String)(implicit ec:ExecutionContext) {

  val parentName = tablePrefix + "parent"
  val childName = tablePrefix + "child"

  private val parentForm = BoxForm_row(
    name = parentName,
    entity = parentName,
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

  private val childForm = BoxForm_row(
    name = childName,
    entity = childName,
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

  private def parentFormFields(parentFormId:Int,childFormId:Int) = Seq(
    BoxField_row(form_id = parentFormId, `type` = JSONFieldTypes.NUMBER, name = "id"),
    BoxField_row(form_id = parentFormId, `type` = JSONFieldTypes.STRING, name = "name"),
    BoxField_row(form_id = parentFormId, `type` = JSONFieldTypes.CHILD, name = "childs",child_form_id = Some(childFormId),masterFields = Some("id"),childFields = Some("parent_id"))
  )

  private def childFormFields(childFormId:Int) = Seq(
    BoxField_row(form_id = childFormId, `type` = JSONFieldTypes.NUMBER, name = "id"),
    BoxField_row(form_id = childFormId, `type` = JSONFieldTypes.STRING, name = "name"),
  )


  def insertForm()(implicit up:UserProfile) = for{
    _ <- up.boxDb.run(BoxFormTable.filter(x => x.name === parentName || x.name === childName ).delete)
    parentId <- up.boxDb.run( (BoxFormTable returning BoxFormTable.map(_.form_id)) += parentForm)
    childId <- up.boxDb.run( (BoxFormTable returning BoxFormTable.map(_.form_id)) += childForm)
    _ <- up.boxDb.run(DBIO.sequence(parentFormFields(parentId,childId).map(x => BoxFieldTable += x)))
    _ <- up.boxDb.run(DBIO.sequence(childFormFields(childId).map(x => BoxFieldTable += x)))
  } yield {
    parentForm.name
  }
}

object AppManagedIdFixtures{
  val layers = Map(
    1 -> s"""
            |{
            |  "id": 1,
            |  "name": "parent",
            |  "childs" : [
            |  ]
            |}
    """.stripMargin.trim,
    2 -> s"""
            |{
            |  "id": 1,
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "id": 1,
            |       "name": "child",
            |       "parent_id": 1
            |     }
            |  ]
            |}
    """.stripMargin.trim,
    3 -> s"""
            |{
            |  "id": 1,
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "id": 1,
            |       "name": "child",
            |       "parent_id": 1,
            |       "subchilds": [
            |         {
            |           "id": 1,
            |           "name": "subchild",
            |           "child_id": 1
            |         }
            |       ]
            |     }
            |  ]
            |}
    """.stripMargin.trim
  )
}

object DbManagedIdFixtures{
  val layers = Map(
    1 -> s"""
            |{
            |  "name": "parent",
            |  "childs" : [
            |  ]
            |}
    """.stripMargin.trim,
    2 -> s"""
            |{
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "name": "child"
            |     }
            |  ]
            |}
    """.stripMargin.trim,
    3 -> s"""
            |{
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "name": "child",
            |       "subchilds": [
            |         {
            |           "name": "subchild",
            |         }
            |       ]
            |     }
            |  ]
            |}
    """.stripMargin.trim
  )
}
