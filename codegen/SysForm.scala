package ch.wsl.codegen

import scala.slick.driver.PostgresDriver
import PostgresDriver.simple._

case class SysFormRow(id: Option[Int], name: String, table_name:String, json_form: String)

class SysForm(tag: Tag) extends Table[SysFormRow](tag, "sys_form") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def table_name = column[String]("table_name")
  def json_form = column[String]("json_form",O.DBType("TEXT")) // should countain long texts
  def * = (id.?, name, table_name, json_form) <> (SysFormRow.tupled, SysFormRow.unapply)
}