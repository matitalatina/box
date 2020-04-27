package ch.wsl.box.model.boxentities

import ch.wsl.box.jdbc.PostgresProfile.api._

object Schema {
  val box =
    AccessLevel.table.schema ++
      Conf.table.schema ++
      Export.Export.schema ++
      Export.Export_i18n.schema ++
      ExportField.ExportField.schema ++
      ExportField.ExportField_i18n.schema ++
      ExportField.ExportHeader_i18n.schema ++
      Field.table.schema ++
      Field.FieldFile.schema ++
      Field.Field_i18n.schema ++
      Form.table.schema ++
      Form.Form_i18n.schema ++
      Labels.table.schema ++
      UITable.table.schema ++
      UIsrcTable.table.schema ++
      User.table.schema ++
      Function.Function.schema ++
      Function.Function_i18n.schema ++
      Function.FunctionField.schema ++
      Function.FunctionField_i18n.schema ++
      Log.table.schema
}
