package ch.wsl.box.model.shared

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
                      file: Option[FieldFile] = None
                    )


case class JSONFieldLookup(lookupEntity:String, map:JSONFieldMap, lookup:Map[String,String] = Map())

case class FieldFile(file:String,name:String,thumbnail:Option[String])

case class JSONFieldMap(valueProperty:String, textProperty:String)

case class Child(objId:Int, key:String, masterFields:String, childFields:String, childFilter:Seq[JSONQueryFilter])

object JSONFieldTypes{
  val NUMBER = "number"
  val STRING = "string"
  val CHILD = "child"
  val FILE = "file"
}
