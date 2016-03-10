package ch.wsl.jsonmodels

/**
  * Created by andreaminetti on 10/03/16.
  */
case class TitleMap(value:String,name:String)

case class JSONField(
                      `type`:String,
                      table: String,
                      key:String,
                      title:Option[String] = None,
                      titleMap:Option[List[TitleMap]] = None,
                      options:Option[JSONFieldOptions] = None,
                      placeholder:Option[String] = None,
                      widget: Option[String] = None
                    )

case class JSONFieldOptions(async:JSONFieldHTTPOption, map:JSONFieldMap)

case class JSONFieldMap(valueProperty:String,textProperty:String)

case class JSONFieldHTTPOption(url:String)