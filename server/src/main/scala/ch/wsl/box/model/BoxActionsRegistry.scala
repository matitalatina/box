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
    case "access_level" => JSONTableActions[AccessLevel.AccessLevel,AccessLevel.AccessLevel_row](AccessLevel.table)
    case "conf" => JSONTableActions[Conf.Conf,Conf.Conf_row](Conf.table)
    case "export" => JSONTableActions[Export.Export,Export.Export_row](Export.Export)
    case "export_field" => JSONTableActions[ExportField.ExportField,ExportField.ExportField_row](ExportField.ExportField)
    case "export_field_i18n" => JSONTableActions[ExportField.ExportField_i18n,ExportField.ExportField_i18n_row](ExportField.ExportField_i18n)
    case "export_header_i18n" => JSONTableActions[ExportField.ExportHeader_i18n,ExportField.ExportHeader_i18n_row](ExportField.ExportHeader_i18n)
    case "export_i18n" => JSONTableActions[Export.Export_i18n,Export.Export_i18n_row](Export.Export_i18n)
    case "field" => JSONTableActions[Field.Field,Field.Field_row](Field.table)
    case "field_file" => JSONTableActions[Field.FieldFile,Field.FieldFile_row](Field.FieldFile)
    case "field_i18n" => JSONTableActions[Field.Field_i18n,Field.Field_i18n_row](Field.Field_i18n)
    case "form" => JSONTableActions[Form.Form,Form.Form_row](Form.table)
    case "form_i18n" => JSONTableActions[Form.Form_i18n,Form.Form_i18n_row](Form.Form_i18n)
    case "labels" => JSONTableActions[Labels.Labels,Labels.Labels_row](Labels.table)
    case "ui" => JSONTableActions[UITable.UI,UITable.UI_row](UITable.table)
    case "ui_src" => JSONTableActions[UIsrcTable.UIsrc,UIsrcTable.UIsrc_row](UIsrcTable.table)
    case "users" => JSONTableActions[User.User,User.User_row](User.table)
    case "function" => JSONTableActions[Function.Function,Function.Function_row](Function.Function)
    case "function_i18n" => JSONTableActions[Function.Function_i18n,Function.Function_i18n_row](Function.Function_i18n)
    case "function_field" => JSONTableActions[Function.FunctionField,Function.FunctionField_row](Function.FunctionField)
    case "function_field_i18n" => JSONTableActions[Function.FunctionField_i18n,Function.FunctionField_i18n_row](Function.FunctionField_i18n)
    case s:String => Registry().actions.tableActions(ec)(s)
  }

}

object BoxActionsRegistry{
  def apply()(implicit ec: ExecutionContext) = new BoxActionsRegistry
}

