package ch.wsl.box.model

import ch.wsl.box.rest.runtime.{ColType, FieldRegistry}

object BoxFieldAccessRegistry extends FieldRegistry {


  def field(table: String, column: String): ColType = {

    val tableFields: Map[String, ColType] = table match {
      case "access_level" => Map(
        "access_level_id" -> ColType("Int", false),
        "access_level" -> ColType("String", false)
      )
      case "conf" => Map(
        "id" -> ColType("Int", false),
        "key" -> ColType("String", false),
        "value" -> ColType("String", true)
      )
      case "export" => Map(
        "export_id" -> ColType("Int", false),
        "name" -> ColType("String", false),
        "function" -> ColType("String", false),
        "description" -> ColType("String", true),
        "layout" -> ColType("String", true),
        "parameters" -> ColType("String", true),
        "order" -> ColType("Double", true),
        "access_role" -> ColType("scala.collection.Seq", true)
      )
      case "export_field" => Map(
        "field_id" -> ColType("Int", false),
        "export_id" -> ColType("Int", false),
        "type" -> ColType("String", false),
        "name" -> ColType("String", false),
        "widget" -> ColType("String", true),
        "lookupEntity" -> ColType("String", true),
        "lookupValueField" -> ColType("String", true),
        "lookupQuery" -> ColType("String", true),
        "default" -> ColType("String", true),
        "conditionFieldId" -> ColType("String", true),
        "conditionValues" -> ColType("String", true)
      )
      case "export_field_i18n" => Map(
        "id" -> ColType("Int", false),
        "field_id" -> ColType("Int", true),
        "lang" -> ColType("String", true),
        "label" -> ColType("String", true),
        "placeholder" -> ColType("String", true),
        "tooltip" -> ColType("String", true),
        "hint" -> ColType("String", true),
        "lookupTextField" -> ColType("String", true)
      )
      case "export_header_i18n" => Map(
        "id" -> ColType("Int", false),
        "key" -> ColType("String", false),
        "lang" -> ColType("String", false),
        "label" -> ColType("String", false)
      )
      case "export_i18n" => Map(
        "id" -> ColType("Int", false),
        "export_id" -> ColType("Int", true),
        "lang" -> ColType("String", true),
        "label" -> ColType("String", true),
        "tooltip" -> ColType("String", true),
        "hint" -> ColType("String", true),
        "function" -> ColType("String", true)
      )
      case "field" => Map(
        "field_id" -> ColType("Int", false),
        "form_id" -> ColType("Int", false),
        "type" -> ColType("String", false),
        "name" -> ColType("String", false),
        "widget" -> ColType("String", true),
        "lookupEntity" -> ColType("String", true),
        "lookupValueField" -> ColType("String", true),
        "lookupQuery" -> ColType("String", true),
        "child_form_id" -> ColType("Int", true),
        "masterFields" -> ColType("String", true),
        "childFields" -> ColType("String", true),
        "childQuery" -> ColType("String", true),
        "default" -> ColType("String", true),
        "conditionFieldId" -> ColType("String", true),
        "conditionValues" -> ColType("String", true)
      )
      case "field_file" => Map(
        "field_id" -> ColType("Int", false),
        "file_field" -> ColType("String", false),
        "thumbnail_field" -> ColType("String", true),
        "name_field" -> ColType("String", false)
      )
      case "field_i18n" => Map(
        "id" -> ColType("Int", false),
        "field_id" -> ColType("Int", true),
        "lang" -> ColType("String", true),
        "label" -> ColType("String", true),
        "placeholder" -> ColType("String", true),
        "tooltip" -> ColType("String", true),
        "hint" -> ColType("String", true),
        "lookupTextField" -> ColType("String", true)
      )
      case "form" => Map(
        "form_id" -> ColType("Int", false),
        "name" -> ColType("String", false),
        "entity" -> ColType("String", false),
        "description" -> ColType("String", true),
        "layout" -> ColType("String", true),
        "tabularFields" -> ColType("String", true),
        "query" -> ColType("String", true),
        "exportfields" -> ColType("String", true)
      )
      case "form_i18n" => Map(
        "id" -> ColType("Int", false),
        "form_id" -> ColType("Int", true),
        "lang" -> ColType("String", true),
        "label" -> ColType("String", true),
        "tooltip" -> ColType("String", true),
        "hint" -> ColType("String", true)
      )
      case "function" => Map(
        "function_id" -> ColType("Int", false),
        "name" -> ColType("String", false),
        "mode" -> ColType("String", false),
        "function" -> ColType("String", false),
        "presenter" -> ColType("String", true),
        "description" -> ColType("String", true),
        "layout" -> ColType("String", true),
        "order" -> ColType("Double", true),
        "access_role" -> ColType("scala.collection.Seq", true)
      )
      case "function_field" => Map(
        "field_id" -> ColType("Int", false),
        "function_id" -> ColType("Int", false),
        "type" -> ColType("String", false),
        "name" -> ColType("String", false),
        "widget" -> ColType("String", true),
        "lookupEntity" -> ColType("String", true),
        "lookupValueField" -> ColType("String", true),
        "lookupQuery" -> ColType("String", true),
        "default" -> ColType("String", true),
        "conditionFieldId" -> ColType("String", true),
        "conditionValues" -> ColType("String", true)
      )
      case "function_field_i18n" => Map(
        "id" -> ColType("Int", false),
        "field_id" -> ColType("Int", true),
        "lang" -> ColType("String", true),
        "label" -> ColType("String", true),
        "placeholder" -> ColType("String", true),
        "tooltip" -> ColType("String", true),
        "hint" -> ColType("String", true),
        "lookupTextField" -> ColType("String", true)
      )
      case "function_i18n" => Map(
        "id" -> ColType("Int", false),
        "function_id" -> ColType("Int", true),
        "lang" -> ColType("String", true),
        "label" -> ColType("String", true),
        "tooltip" -> ColType("String", true),
        "hint" -> ColType("String", true),
        "function" -> ColType("String", true)
      )
      case "labels" => Map(
        "id" -> ColType("Int", false),
        "lang" -> ColType("String", false),
        "key" -> ColType("String", false),
        "label" -> ColType("String", true)
      )
      case "log" => Map(
        "id" -> ColType("Int", false),
        "filename" -> ColType("String", true),
        "classname" -> ColType("String", true),
        "line" -> ColType("Int", true),
        "message" -> ColType("String", true),
        "timestamp" -> ColType("Int", true)
      )
      case "ui" => Map(
        "id" -> ColType("Int", false),
        "key" -> ColType("String", false),
        "value" -> ColType("String", false),
        "access_level_id" -> ColType("Int", false)
      )
      case "ui_src" => Map(
        "id" -> ColType("Int", false),
        "file" -> ColType("Array[Byte]", true),
        "mime" -> ColType("String", true),
        "name" -> ColType("String", true),
        "access_level_id" -> ColType("Int", false)
      )
      case "users" => Map(
        "username" -> ColType("String", false),
        "access_level_id" -> ColType("Int", false)
      )
      case "v_roles" => Map(
        "rolname" -> ColType("String", true),
        "rolsuper" -> ColType("Boolean", true),
        "rolinherit" -> ColType("Boolean", true),
        "rolcreaterole" -> ColType("Boolean", true),
        "rolcreatedb" -> ColType("Boolean", true),
        "rolcanlogin" -> ColType("Boolean", true),
        "rolconnlimit" -> ColType("Int", true),
        "rolvaliduntil" -> ColType("java.sql.Timestamp", true),
        "memberof" -> ColType("String", true),
        "rolreplication" -> ColType("Boolean", true),
        "rolbypassrls" -> ColType("Boolean", true)
      )
      case _ => Map()
    }

    tableFields.get(column).getOrElse(ColType("Unknown", false))

  }

}
