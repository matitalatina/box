package ch.wsl.box.testmodel

import ch.wsl.box.rest.runtime.{ColType, FieldRegistry}
import scribe.Logging

object TestFieldRegistry extends FieldRegistry with Logging {
  override def field(table: String, column: String): ColType = {
    val tableFields:Map[String,ColType] = table match {
      case "simple" => Map(
        "id" -> ColType("Int", true),
        "name" -> ColType("String", true),
      )
      case "parent" => Map(
        "id" -> ColType("Int", true),
        "name" -> ColType("String", true)
      )
      case "child" => Map(
        "id" -> ColType("Int", true),
        "name" -> ColType("String", true),
        "parent_id" -> ColType("Int", true)
      )
      case "subchild" => Map(
        "id" -> ColType("Int", true),
        "name" -> ColType("String", true),
        "child_id" -> ColType("Int", true)
      )
      case _ => {
        logger.warn(s"Not found table $table")
        Map()
      }
    }

    tableFields.getOrElse(column,{
      logger.warn(s"Not found column $column in table $table")
      ColType("Unknown", false)
    })

  }
}
