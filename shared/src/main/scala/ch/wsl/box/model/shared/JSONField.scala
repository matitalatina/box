package ch.wsl.box.model.shared

import io.circe.Json

/**
  * Created by andreaminetti on 16/03/16.
  */
case class JSONField(
                      `type`:String,
                      name:String,
                      nullable: Boolean,
                      readOnly: Boolean = false,
                      label:Option[String] = None,
                      lookup:Option[JSONFieldLookup] = None,
                      placeholder:Option[String] = None,
                      widget: Option[String] = None,
                      child: Option[Child] = None,
                      default: Option[String] = None,
                      file: Option[FileReference] = None,
                      condition: Option[ConditionalField] = None,
                      tooltip: Option[String] = None,
                      params: Option[Json] = None,
                      linked: Option[LinkedForm] = None,
                      lookupLabel: Option[LookupLabel] = None
                    ) {
  def title = label.getOrElse(name)
}

object JSONField{
  val empty = JSONField("","",true,true)
  val fullWidth = empty.copy(params = Some(Json.obj("fullWidth" -> Json.True)))
}

case class LinkedForm(name:String,parentValueFields:Seq[String], childValueFields:Seq[String], lookup:Option[LookupLabel],label:Option[String])

case class LookupLabel(localIds:Seq[String],remoteIds:Seq[String],remoteField:String,remoteEntity:String,widget:String)
/**
  *
  * @param lookupEntity
  * @param map
  * @param lookup
  * @param lookupQuery
  * @param lookupExtractor map with on the first place the key of the Json, on second place the possible values with they respective values
  */
case class JSONFieldLookup(lookupEntity:String, map:JSONFieldMap, lookup:Seq[JSONLookup] = Seq(), lookupQuery:Option[String] = None, lookupExtractor: Option[JSONLookupExtractor] = None)

case class JSONLookupExtractor(key:String, values:Seq[Json], results:Seq[Seq[JSONLookup]]) {
  def map = values.zip(results).toMap
}


object JSONFieldLookup {
  val empty: JSONFieldLookup = JSONFieldLookup("",JSONFieldMap("","", ""))

  def fromData(lookupEntity:String, mapping:JSONFieldMap, lookupData:Seq[Json], lookupQuery:Option[String] = None):JSONFieldLookup = {
    import ch.wsl.box.shared.utils.JSONUtils._

    val options = lookupData.map{ lookupRow =>

      val label = mapping.textProperty.split(",").map(_.trim).map(k => lookupRow.get(k)).mkString(" - ")

      JSONLookup(lookupRow.get(mapping.valueProperty),label)
    }
    JSONFieldLookup(lookupEntity, mapping, options,lookupQuery)
  }

  def prefilled(data:Seq[JSONLookup]) = JSONFieldLookup("",JSONFieldMap("","", ""),data)
  def withExtractor(key:String,extractor:Map[Json,Seq[JSONLookup]]) = {
    val extractorSeq = extractor.toSeq
    JSONFieldLookup("",JSONFieldMap("","", ""),Seq(),None,Some(JSONLookupExtractor(
      key,
      extractorSeq.map(_._1),
      extractorSeq.map(_._2)
    )))
  }
}

case class JSONLookup(id:String, value:String)

case class FileReference(name_field:String, file_field:String, thumbnail_field:Option[String])

case class JSONFieldMap(valueProperty:String, textProperty:String, localValueProperty:String)

case class ChildMapping(parent:String,child:String)

case class Child(objId:Int, key:String, mapping:Seq[ChildMapping], childQuery:Option[JSONQuery])

object Child{
  def apply(objId: Int, key: String, masterFields: String, childFields: String, childQuery: Option[JSONQuery]): Child = {
    val mapping = masterFields.split(",").map(_.trim).zip(childFields.split(",").map(_.trim)).map{ case (p,c) => ChildMapping(p,c)}
    new Child(objId, key, mapping, childQuery)
  }
}

case class ConditionalField(conditionFieldId:String,conditionValues:Seq[Json])

object JSONFieldTypes{
  val NUMBER = "number"
  val STRING = "string"
  val CHILD = "child"
  val FILE = "file"
  val DATE = "date"
  val DATETIME = "datetime"
  val TIME = "time"
  val INTERVAL = "interval" //Not used
  val BOOLEAN = "boolean"
  val ARRAY_NUMBER = "array_number"
  val ARRAY_STRING = "array_string"
  val GEOMETRY = "geometry"
  val JSON = "json"
  val STATIC = "static"

  val ALL = Seq(NUMBER,STRING,FILE,DATE,DATETIME,TIME, BOOLEAN, ARRAY_NUMBER, ARRAY_STRING,CHILD,GEOMETRY,JSON,STATIC)
}
