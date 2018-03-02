package ch.wsl.box.rest.model

import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.rest.boxentities._
import ch.wsl.box.rest.boxentities.Conf.{Conf, Conf_row}
import ch.wsl.box.rest.boxentities.Field.{Field, Field_i18n, Field_i18n_row, Field_row}
import ch.wsl.box.rest.boxentities.Form.{Form, Form_row}
import ch.wsl.box.rest.boxentities.Labels.{Labels, Labels_row}
import ch.wsl.box.rest.boxentities.UITable.{UI, UI_row}
import ch.wsl.box.rest.logic.JsonTableActions

import scala.concurrent.ExecutionContext

case class BoxTablesRegistry(implicit ec: ExecutionContext) extends EntityActionsRegistry {

  import io.circe._
  import io.circe.generic.auto._
  import ch.wsl.box.rest.utils.JSONSupport._


  override def tableActions = {
    case "conf" => JsonTableActions[Conf,Conf_row](Conf.table)
    case "field" => JsonTableActions[Field,Field_row](Field.table)
    case "field_i18n" => JsonTableActions[Field_i18n,Field_i18n_row](Field_i18n)
    case "form" => JsonTableActions[Form,Form_row](Form.table)
    case "labels" => JsonTableActions[Labels,Labels_row](Labels.table)
    case "ui" => JsonTableActions[UI,UI_row](UITable.table)
    case s:String => super.tableActions(s)
  }
}
