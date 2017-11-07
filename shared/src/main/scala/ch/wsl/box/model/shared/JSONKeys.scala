package ch.wsl.box.model.shared

import ch.wsl.box.shared.utils.JsonUtils._
import io.circe.Json

/**
  * Created by andreaminetti on 18/03/16.
  */
case class JSONKeys(keys:Vector[JSONKey]) {
  def asString = keys.map(k => k.key + "::" + k.value).mkString(",")

  def values: Vector[String] = keys.map(_.value)

  def query:JSONQuery = JSONQuery(None,List(),keys.map(_.filter).toList)

}

object JSONKeys {
  def empty = JSONKeys(Vector())
  def fromString(str:String) = JSONKeys(
    str.split(",").map{ k =>
      val c = k.split("::")
      JSONKey(c(0),c(1))
    }.toVector
  )

  def fromMap(keys:Map[String,String]) = {
    val jsonKeys = keys.map{ case (k,v) => JSONKey(k,v)}
    JSONKeys(jsonKeys.toVector)
  }

  def fromMap(keys:Seq[(String,Json)]):JSONKeys = {
    JSONKeys(keys.map{ case (k,v) => JSONKey(k,v.string)}.toVector)
  }

}

case class JSONKey(key:String,value:String) {
  def filter = JSONQueryFilter(key,Some(Filter.EQUALS),value)
}
