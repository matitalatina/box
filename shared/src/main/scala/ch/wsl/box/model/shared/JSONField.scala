package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 16/03/16.
  */
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

case class TitleMap(value:String,name:String)

case class JSONFieldOptions(async:JSONFieldHTTPOption, map:JSONFieldMap)

case class JSONFieldHTTPOption(url:String)

case class JSONFieldMap(valueProperty:String, textProperty:String)