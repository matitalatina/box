package ch.wsl.box.testmodel

import ch.wsl.box.rest.runtime.{ColType, FieldRegistry}
import scribe.Logging

object TestFieldRegistry extends FieldRegistry with Logging {


  override def tables: Seq[String] = Seq("simple")

  override def views: Seq[String] = Seq()

  val tableFields:Map[String,Map[String,ColType]] = Map(
      "simple"-> Map(
        "id" -> ColType("Int", "number", true),
        "name" -> ColType("String", "string", true),
      ),
      "app_parent"-> Map(
        "id" -> ColType("Int", "number", true),
        "name" -> ColType("String", "string", true)
      ),
      "app_child"-> Map(
        "id" -> ColType("Int", "number", true),
        "name" -> ColType("String", "string", true),
        "parent_id" -> ColType("Int", "number", true)
      ),
      "app_subchild"-> Map(
        "id" -> ColType("Int", "number", true),
        "name" -> ColType("String", "string", true),
        "child_id" -> ColType("Int", "number", true)
      ),
      "db_parent"-> Map(
        "id" -> ColType("Int", "number", true),
        "name" -> ColType("String", "string", true)
      ),
      "db_child"-> Map(
        "id" -> ColType("Int", "number", true),
        "name" -> ColType("String", "string", true),
        "parent_id" -> ColType("Int", "number", true)
      ),
      "db_subchild"-> Map(
        "id" -> ColType("Int", "number", true),
        "name" -> ColType("String", "string", true),
        "child_id" -> ColType("Int", "number", true)
      )
    )

}
