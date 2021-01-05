package ch.wsl.box.model

import ch.wsl.box.rest.runtime._

object BoxTableRegistry extends TableRegistry {


  def table(name:String):TableRegistryEntry = {

    import ch.wsl.box.jdbc.PostgresProfile.api._

    name match {
      case "access_level" => new TableRegistryEntry{

        type MT = boxentities.BoxAccessLevel.BoxAccessLevel

        override val name = "access_level"
        override val tableQuery = boxentities.BoxAccessLevel.BoxAccessLevelTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "conf" => new TableRegistryEntry{

        type MT = boxentities.BoxConf.BoxConf

        override val name = "conf"
        override val tableQuery = boxentities.BoxConf.BoxConfTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "export" => new TableRegistryEntry{

        type MT =  boxentities.BoxExport.BoxExport

        override val name = "export"
        override val tableQuery = boxentities.BoxExport.BoxExportTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "export_field" => new TableRegistryEntry{

        type MT = boxentities.BoxExportField.BoxExportField

        override val name = "export_field"
        override val tableQuery = boxentities.BoxExportField.BoxExportFieldTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "export_field_i18n" => new TableRegistryEntry{

        type MT = boxentities.BoxExportField.BoxExportField_i18n

        override val name = "export_field_i18n"
        override val tableQuery = boxentities.BoxExportField.BoxExportField_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "export_header_i18n" => new TableRegistryEntry{

        type MT = boxentities.BoxExportField.BoxExportHeader_i18n

        override val name = "export_header_i18n"
        override val tableQuery = boxentities.BoxExportField.BoxExportHeader_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "export_i18n" => new TableRegistryEntry{

        type MT = boxentities.BoxExport.BoxExport_i18n

        override val name = "export_i18n"
        override val tableQuery = boxentities.BoxExport.BoxExport_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "field" => new TableRegistryEntry{

        type MT = boxentities.BoxField.BoxField

        override val name = "field"
        override val tableQuery = boxentities.BoxField.BoxFieldTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "field_file" => new TableRegistryEntry{

        type MT = boxentities.BoxField.BoxFieldFile

        override val name = "field_file"
        override val tableQuery = boxentities.BoxField.BoxFieldFileTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "field_i18n" => new TableRegistryEntry{

        type MT = boxentities.BoxField.BoxField_i18n

        override val name = "field_i18n"
        override val tableQuery = boxentities.BoxField.BoxField_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "form" => new TableRegistryEntry{

        type MT = boxentities.BoxForm.BoxForm

        override val name = "form"
        override val tableQuery = boxentities.BoxForm.BoxFormTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "form_i18n" => new TableRegistryEntry{

        type MT = boxentities.BoxForm.BoxForm_i18n

        override val name = "form_i18n"
        override val tableQuery = boxentities.BoxForm.BoxForm_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "function" => new TableRegistryEntry{

        type MT = boxentities.BoxFunction.BoxFunction

        override val name = "function"
        override val tableQuery = boxentities.BoxFunction.BoxFunctionTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "function_field" => new TableRegistryEntry{

        type MT = boxentities.BoxFunction.BoxFunctionField

        override val name = "function_field"
        override val tableQuery = boxentities.BoxFunction.BoxFunctionFieldTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "function_field_i18n" => new TableRegistryEntry{

        type MT = boxentities.BoxFunction.BoxFunctionField_i18n

        override val name = "function_field_i18n"
        override val tableQuery = boxentities.BoxFunction.BoxFunctionField_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "function_i18n" => new TableRegistryEntry{

        type MT = boxentities.BoxFunction.BoxFunction_i18n

        override val name = "function_i18n"
        override val tableQuery = boxentities.BoxFunction.BoxFunction_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "labels" => new TableRegistryEntry{

        type MT = boxentities.BoxLabels.BoxLabels

        override val name = "labels"
        override val tableQuery = boxentities.BoxLabels.BoxLabelsTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "log" => new TableRegistryEntry{

        type MT = boxentities.BoxLog.BoxLogs

        override val name = "log"
        override val tableQuery = boxentities.BoxLog.BoxLogsTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "news" => new TableRegistryEntry{

        type MT =  boxentities.BoxNews.BoxNews

        override val name = "news"
        override val tableQuery = boxentities.BoxNews.BoxNewsTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "news_i18n" => new TableRegistryEntry{

        type MT =  boxentities.BoxNews.BoxNews_i18n

        override val name = "news_i18n"
        override val tableQuery = boxentities.BoxNews.BoxNews_i18nTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "ui" => new TableRegistryEntry{

        type MT = boxentities.BoxUITable.BoxUI

        override val name = "ui"
        override val tableQuery = boxentities.BoxUITable.BoxUITable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "ui_src" => new TableRegistryEntry{

        type MT = boxentities.BoxUIsrcTable.BoxUIsrc

        override val name = "ui_src"
        override val tableQuery = boxentities.BoxUIsrcTable.BoxUIsrcTable.asInstanceOf[TableQuery[Table[MT]]]


      }
      case "users" => new TableRegistryEntry{

        type MT = boxentities.BoxUser.BoxUser

        override val name = "users"
        override val tableQuery = boxentities.BoxUser.BoxUserTable.asInstanceOf[TableQuery[Table[MT]]]


      }
    }

  }

}


