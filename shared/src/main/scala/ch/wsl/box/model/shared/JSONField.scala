package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 16/03/16.
  */
case class JSONField(
                      `type`:String,
                      key:String,
                      nullable: Boolean,
                      title:Option[String] = None,
                      lookup:Option[JSONFieldOptions] = None,
                      placeholder:Option[String] = None,
                      widget: Option[String] = None,
                      subform: Option[Subform] = None,
                      default: Option[String] = None
                    )


case class JSONFieldOptions(refModel:String, map:JSONFieldMap, options:Map[String,String] = Map())


case class JSONFieldMap(valueProperty:String, textProperty:String)

case class Subform(id:Int,localFields:String,subFields:String,subFilter:Seq[JSONQueryFilter])

object JSONTypes{
  val NUMBER = "number"
  val STRING = "string"
  val SUBFORM = "subform"
}
