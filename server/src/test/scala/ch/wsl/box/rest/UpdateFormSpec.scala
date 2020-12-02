package ch.wsl.box.rest


import ch.wsl.box.jdbc.FullDatabase
import ch.wsl.box.rest.logic.FormActions
import ch.wsl.box.model.shared.{JSONID, JSONKeyValue}
import ch.wsl.box.rest.metadata.{ FormMetadataFactory}
import ch.wsl.box.testmodel.EntityActionsRegistry
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.fixtures.{AppManagedIdFixtures, DbManagedIdFixtures, FormFixtures}
import ch.wsl.box.rest.utils.UserProfile
import io.circe.Json
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
      val subchilds = childs.head.seq("subchilds")
      subchilds.length shouldBe 1
      subchilds.head.get("name") shouldBe "subchild"
    }

  }

}
