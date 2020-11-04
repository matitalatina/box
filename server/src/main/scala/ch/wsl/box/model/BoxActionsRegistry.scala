package ch.wsl.box.model



import scala.concurrent.ExecutionContext
import ch.wsl.box.rest.logic.{JSONTableActions, JSONViewActions, TableActions}
import ch.wsl.box.model.boxentities._
import ch.wsl.box.rest.runtime.Registry

class BoxActionsRegistry(implicit ec:ExecutionContext)  {

  import io.circe._
  import io.circe.generic.auto._
  import ch.wsl.box.rest.utils.JSONSupport._

  def tableActions:String => TableActions[Json] = {
    case "access_level" => JSONTableActions[BoxAccessLevel.BoxAccessLevel,BoxAccessLevel.BoxAccessLevel_row](BoxAccessLevel.BoxAccessLevelTable)
    case "conf" => JSONTableActions[BoxConf.BoxConf,BoxConf.BoxConf_row](BoxConf.BoxConfTable)
    case "export" => JSONTableActions[BoxExport.BoxExport,BoxExport.BoxExport_row](BoxExport.BoxExportTable)
    case "export_field" => JSONTableActions[BoxExportField.BoxExportField,BoxExportField.BoxExportField_row](BoxExportField.BoxExportFieldTable)
    case "export_field_i18n" => JSONTableActions[BoxExportField.BoxExportField_i18n,BoxExportField.BoxExportField_i18n_row](BoxExportField.BoxExportField_i18nTable)
    case "export_header_i18n" => JSONTableActions[BoxExportField.BoxExportHeader_i18n,BoxExportField.BoxExportHeader_i18n_row](BoxExportField.BoxExportHeader_i18nTable)
    case "export_i18n" => JSONTableActions[BoxExport.BoxExport_i18n,BoxExport.BoxExport_i18n_row](BoxExport.BoxExport_i18nTable)
    case "field" => JSONTableActions[BoxField.BoxField,BoxField.BoxField_row](BoxField.BoxFieldTable)
    case "field_file" => JSONTableActions[BoxField.BoxFieldFile,BoxField.BoxFieldFile_row](BoxField.BoxFieldFileTable)
    case "field_i18n" => JSONTableActions[BoxField.BoxField_i18n,BoxField.BoxField_i18n_row](BoxField.BoxField_i18nTable)
    case "form" => JSONTableActions[BoxForm.BoxForm,BoxForm.BoxForm_row](BoxForm.BoxFormTable)
    case "form_i18n" => JSONTableActions[BoxForm.BoxForm_i18n,BoxForm.BoxForm_i18n_row](BoxForm.BoxForm_i18nTable)
    case "labels" => JSONTableActions[BoxLabels.BoxLabels,BoxLabels.BoxLabels_row](BoxLabels.BoxLabelsTable)
    case "ui" => JSONTableActions[BoxUITable.BoxUI,BoxUITable.BoxUI_row](BoxUITable.BoxUITable)
    case "ui_src" => JSONTableActions[BoxUIsrcTable.BoxUIsrc,BoxUIsrcTable.BoxUIsrc_row](BoxUIsrcTable.BoxUIsrcTable)
    case "users" => JSONTableActions[BoxUser.BoxUser,BoxUser.BoxUser_row](BoxUser.BoxUserTable)
    case "function" => JSONTableActions[BoxFunction.BoxFunction,BoxFunction.BoxFunction_row](BoxFunction.BoxFunctionTable)
    case "function_i18n" => JSONTableActions[BoxFunction.BoxFunction_i18n,BoxFunction.BoxFunction_i18n_row](BoxFunction.BoxFunction_i18nTable)
    case "function_field" => JSONTableActions[BoxFunction.BoxFunctionField,BoxFunction.BoxFunctionField_row](BoxFunction.BoxFunctionFieldTable)
    case "function_field_i18n" => JSONTableActions[BoxFunction.BoxFunctionField_i18n,BoxFunction.BoxFunctionField_i18n_row](BoxFunction.BoxFunctionField_i18nTable)
    case s:String => Registry().actions(s)
  }

}

object BoxActionsRegistry{
  def apply()(implicit ec: ExecutionContext) = new BoxActionsRegistry
}

