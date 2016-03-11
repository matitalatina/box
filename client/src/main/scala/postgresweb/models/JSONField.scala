package postgresweb.models

/**
  * Created by andreaminetti on 23/02/16.
  */

case class TitleMap(value:String,name:String)

case class JSONField(
                      `type`:String,
                      key:String,
                      title:Option[String] = None,
                      titleMap:Option[List[TitleMap]] = None,
                      options:Option[JSONFieldOptions] = None,
                      placeholder:Option[String] = None
                    )

case class JSONFieldOptions(async:JSONFieldHTTPOption, map:JSONFieldMap)

case class JSONFieldMap(valueProperty:String,textProperty:String)

case class JSONFieldHTTPOption(url:String)