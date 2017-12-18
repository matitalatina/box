package ch.wsl.box.model.shared

import ch.wsl.box.shared.utils.JsonUtils._
import io.circe.Json

/**
  * Created by andreaminetti on 18/03/16.
  */
case class JSONIDs(ids:Vector[JSONID]) {
  def asString = ids.map(k => k.key + "::" + k.value).mkString(",")

  def values: Vector[String] = ids.map(_.value)

  def query:JSONQuery = JSONQuery.empty.copy(filter=ids.map(_.filter).toList)

}

object JSONIDs {
  def empty = JSONIDs(Vector())

  def fromString(str:String) = JSONIDs(
    str.split(",").map{ k =>
      val c = k.split("::")
      JSONID(c(0),c(1))
    }.toVector
  )

  def fromMap(ids:Map[String,String]) = {
    val jsonIds = ids.map{ case (k,v) => JSONID(k,v)}
    JSONIDs(jsonIds.toVector)
  }

  def fromMap(ids:Seq[(String,Json)]):JSONIDs = {
    JSONIDs(ids.map{ case (k,v) => JSONID(k,v.string)}.toVector)
  }

}

case class JSONID(key:String, value:String) {
  def filter = JSONQueryFilter(key,Some(Filter.EQUALS),value)
}
