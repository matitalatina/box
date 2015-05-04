package ch.wsl.rest.domain

import com.typesafe.config._
import net.ceedubs.ficus.Ficus._


case class TitleMap(value:String,name:String)

case class JSONField(
  `type`:String,
  key:Option[String] = None,
  title:Option[String] = None,
  titleMap:Option[List[TitleMap]] = None,
  options:Option[JSONFieldOptions] = None,
  placeholder:Option[String] = None
)

case class JSONFieldOptions(async:JSONFieldHTTPOption, map:JSONFieldMap)

case class JSONFieldMap(valueProperty:String,textProperty:String)

case class JSONFieldHTTPOption(url:String)



object JSONForm {
  
  type JSONForm = List[JSONField]
  
  val tableFieldTitles: Config = ConfigFactory.load().as[Config]("rest.lookup.labels")
  
  def of(table:String):JSONForm = {
    
    val schema = new PgSchema(table)
    
    println(schema.fk)
    
    var constraints = List[String]()
    
    def field2form(field:PgColumn):Option[JSONField] = schema.findFk(field.column_name) match {
      case Some(fk) => {
        if(constraints.contains(fk.contraintName)) {
          None
        } else {
          constraints = fk.contraintName :: constraints
          
          val title = tableFieldTitles.as[Option[String]](fk.referencingTable).getOrElse("en")
          
          Some(JSONField(
              JSONSchema.typesMapping(field.data_type),
              key = Some(field.column_name),
              placeholder = Some(fk.referencingTable + " Lookup"),
              options = Some(
                 JSONFieldOptions(JSONFieldHTTPOption("http://localhost:8080/"+fk.referencingTable),JSONFieldMap(fk.referencingKeys.head,title))
              )
          ))
        }
      }
      case _ => Some(JSONField(JSONSchema.typesMapping(field.data_type),key = Some(field.column_name)))
    }
    
    schema.columns.flatMap(field2form)
    
  }
  
  
  
  
  
  
  
}