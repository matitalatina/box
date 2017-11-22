package ch.wsl.box.codegen

import slick.codegen.SourceCodeGenerator

trait BoxSourceCodeGenerator extends SourceCodeGenerator {
  override def tableName = (dbName: String) => dbName.capitalize
  /** Maps database table name to entity case class name
    *
    *@group Basic customization overrides */
  override def entityName = (dbName: String) => dbName.capitalize+"_row"
}
