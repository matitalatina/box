package ch.wsl.box.model.shared

import io.circe.Json

/**
  * Created by andreaminetti on 16/03/16.
  */
case class JSONField(
                      `type`:String,
                      name:String,
                      nullable: Boolean,
                      label:Option[String] = None,
                      lookup:Option[JSONFieldLookup] = None,
                      placeholder:Option[String] = None,
                      widget: Option[String] = None,
                      child: Option[Child] = None,
                      default: Option[String] = None,
                      file: Option[FileReference] = None,
                      condition: Option[ConditionalField] = None,
                      tooltip: Option[String] = None
                    ) {
  def title = label.getOrElse(name)
}

object JSONField{
  val empty = JSONField("","",true)
}


case class JSONFieldLookup(lookupEntity:String, map:JSONFieldMap, lookup:Seq[JSONLookup] = Seq())

object JSONFieldLookup {
  val empty: JSONFieldLookup = JSONFieldLookup("",JSONFieldMap("",""))

  def fromData(lookupEntity:String, mapping:JSONFieldMap, lookupData:Seq[Json]):JSONFieldLookup = {
    import ch.wsl.box.shared.utils.JSONUtils._

    val options = lookupData.map{ lookupRow =>
      JSONLookup(lookupRow.get(mapping.valueProperty),lookupRow.get(mapping.textProperty))
    }
    JSONFieldLookup(lookupEntity, mapping, options)
  }
}

case class JSONLookup(id:String, value:String)

case class FileReference(name_field:String, file_field:String, thumbnail_field:Option[String])

case class JSONFieldMap(valueProperty:String, textProperty:String)

case class Child(objId:Int, key:String, masterFields:String, childFields:String, childFilter:Seq[JSONQueryFilter])

case class ConditionalField(conditionFieldId:String,conditionValues:Seq[Json])

object JSONFieldTypes{
  val NUMBER = "number"
  val STRING = "string"
  val CHILD = "child"
  val FILE = "file"
  val DATE = "date"
  val DATETIME = "datetime"
  val TIME = "time"
  val BOOLEAN = "boolean"
  val ARRAY_NUMBER = "array_number"
  val ARRAY_STRING = "array_string"

  val ALL = Seq(NUMBER,STRING,FILE,DATE,DATETIME,TIME, BOOLEAN, ARRAY_NUMBER, ARRAY_STRING)
}
