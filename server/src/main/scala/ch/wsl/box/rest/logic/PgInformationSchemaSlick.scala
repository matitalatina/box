package ch.wsl.box.rest.logic

import slick.driver.PostgresDriver.api._
import StringHelper._

case class PgTable(
   table_schema:String,
   table_name:String,
   table_type:String,
   is_insertable_into:String){

  def isView = table_type == "VIEW"
  def isTable = table_type == "BASE_TABLE"
  def isInsertableInto = is_insertable_into == "YES"
  def boxName = table_name.slickfy
}

class PgTables(tag: Tag) extends Table[PgTable](tag,  Some("information_schema"), "tables") {

  def table_schema = column[String]("table_schema")
  def table_name = column[String]("table_name")
  def table_type = column[String]("table_type")
  def is_insertable_into = column[String]("is_insertable_into")

  def * = (table_schema, table_name, table_type, is_insertable_into) <> (PgTable.tupled, PgTable.unapply)
}


case class PgColumn(
  column_name:String,
  is_nullable:String,
  is_updatable:String,
  data_type:String,
  character_maximum_length:Option[Int],
  numeric_precision:Option[Int],
  numeric_scale:Option[Int],
  table_name:String,
  table_schema:String,
  ordinal_position:Int
) {
  def nullable = is_nullable == "YES"
  def updatable = is_updatable == "YES"
  def jsonType = JSONMetadataFactory.typesMapping(data_type)
  def defaultWidget = JSONMetadataFactory.defaultWidgetMapping(data_type)
  def boxName = column_name.slickfy
}

class PgColumns(tag: Tag) extends Table[PgColumn](tag,  Some("information_schema"), "columns") {

  def column_name = column[String]("column_name")
  def is_nullable = column[String]("is_nullable")
  def is_updatable = column[String]("is_updatable")
  def data_type = column[String]("data_type")
  def character_maximum_length = column[Option[Int]]("character_maximum_length")
  def numeric_precision = column[Option[Int]]("numeric_precision")
  def numeric_scale = column[Option[Int]]("numeric_scale")
  def table_name = column[String]("table_name")
  def table_schema = column[String]("table_schema")
  def ordinal_position = column[Int]("ordinal_position")
    
  def * = (column_name, is_nullable, is_updatable, data_type, character_maximum_length, numeric_precision, numeric_scale, table_name, table_schema, ordinal_position) <> (PgColumn.tupled, PgColumn.unapply)
}

case class PgConstraint(
  table_name:String,
  constraint_name:String,
  constraint_type:String
)

class PgConstraints(tag: Tag) extends Table[PgConstraint](tag,  Some("information_schema"), "table_constraints") {

  def table_name = column[String]("table_name")
  def constraint_name = column[String]("constraint_name")
  def constraint_type = column[String]("constraint_type")

    
  def * = (table_name, constraint_name, constraint_type) <> (PgConstraint.tupled, PgConstraint.unapply)
}

case class PgConstraintReference(
  constraint_name:String,
  referencing_constraint_name:String
)

class PgConstraintReferences(tag: Tag) extends Table[PgConstraintReference](tag,  Some("information_schema"), "referential_constraints") {

  def constraint_name = column[String]("constraint_name")
  def referencing_constraint_name = column[String]("unique_constraint_name")

    
  def * = (constraint_name, referencing_constraint_name) <> (PgConstraintReference.tupled, PgConstraintReference.unapply)
}

case class PgConstraintUsage(
  constraint_name:String,
  table_name:String,
  column_name:String
)

class PgConstraintUsages(tag: Tag) extends Table[PgConstraintUsage](tag,  Some("information_schema"), "constraint_column_usage") {

  def constraint_name = column[String]("constraint_name")
  def table_name = column[String]("table_name")
  def column_name = column[String]("column_name")

    
  def * = (constraint_name, table_name, column_name) <> (PgConstraintUsage.tupled, PgConstraintUsage.unapply)
}

case class PgKeyUsage(
  constraint_name:String,
  table_name:String,
  column_name:String
)

class PgKeyUsages(tag: Tag) extends Table[PgKeyUsage](tag,  Some("information_schema"), "key_column_usage") {

  def constraint_name = column[String]("constraint_name")
  def table_name = column[String]("table_name")
  def column_name = column[String]("column_name")

    
  def * = (constraint_name, table_name, column_name) <> (PgKeyUsage.tupled, PgKeyUsage.unapply)
}







