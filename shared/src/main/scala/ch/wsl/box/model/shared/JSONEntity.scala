package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 18/03/16.
  */
case class JSONEntity(schema: JSONSchema, form: Seq[JSONField], keys:Seq[String]) {
  def keyOf(row:Vector[(String,String)]):JSONKeys = {
    println(keys)
    JSONKeys(keys.map{ k =>
      JSONKey(k,row.find(_._1 == k).map(_._2).get)
    }.toVector)
  }
}

object JSONEntity{
  def empty = JSONEntity(JSONSchema.empty,Seq(),Seq())
}