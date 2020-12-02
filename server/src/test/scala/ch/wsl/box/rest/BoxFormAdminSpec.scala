package ch.wsl.box.rest


import ch.wsl.box.jdbc.FullDatabase
import ch.wsl.box.rest.logic.FormActions
import ch.wsl.box.rest.metadata.BoxFormMetadataFactory
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.model.BoxActionsRegistry
import ch.wsl.box.model.boxentities.BoxField
import ch.wsl.box.model.shared.JSONID
import ch.wsl.box.rest.fixtures.FormFixtures

import io.circe._, io.circe.syntax._

import ch.wsl.box.shared.utils.JSONUtils._

class BoxFormAdminSpec extends BaseSpec {


  "Admin Box schema form" should "handled" in withUserProfile { implicit up =>

    implicit val bdb = FullDatabase(up.boxDb,up.db)

    for{
      _ <- new FormFixtures("db_").insertForm()
      form <- BoxFormMetadataFactory().of("Interface builder","it")
      actions = FormActions(form,BoxActionsRegistry.apply.tableActions,BoxFormMetadataFactory())
      f <- up.boxDb.run(actions.getById(JSONID.fromMap(Map("form_id" -> "1" ))))
      fieldsBefore <- up.boxDb.run(BoxField.BoxFieldTable.length.result)
      updatedForm = f.get.hcursor.downField("fields").set(f.get.seq("fields").tail.asJson).top.get
      _ <- up.boxDb.run(actions.updateIfNeeded(JSONID.fromData(f.get,form).get,updatedForm))
      fieldsAfter <- up.boxDb.run(BoxField.BoxFieldTable.length.result)
    } yield fieldsBefore should be > fieldsAfter

  }


}


