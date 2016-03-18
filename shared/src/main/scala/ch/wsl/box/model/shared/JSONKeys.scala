package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 18/03/16.
  */
case class JSONKeys(keys:Vector[JSONKey]) {
  def asString = keys.map(k => k.key + "::" + k.value).mkString(",")
}

object JSONKeys {
  def empty = JSONKeys(Vector())
  def fromString(str:String) = JSONKeys(
    str.split(",").map{ k =>
      val c = k.split("::")
      JSONKey(c(0),c(1))
    }.toVector
  )
}

case class JSONKey(key:String,value:String)
