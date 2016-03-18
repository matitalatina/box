package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 18/03/16.
  */
case class JSONModel(schema: JSONSchema, form: Seq[JSONField], keys:Seq[String]) {
  def keyOf(row:Vector[(String,String)]):JSONKeys = {
    JSONKeys(keys.map{ k =>
      JSONKey(k,row.find(_._1 == k).map(_._2).get)
    }.toVector)
  }
}

object JSONModel{
  def empty = JSONModel(JSONSchema.empty,Seq(),Seq())
}