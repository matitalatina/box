package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 16/03/16.
  */

case class JSONSchema(
                       `type`:String,
                       title:Option[String],
                       properties: Map[String,JSONSchemaL2],
                       required: Option[Seq[String]],
                       readonly: Option[Boolean],
                       enum: Option[Seq[String]],
                       order: Option[Int]
                     ) {

  def typeOfTitle:Map[String,String] = properties.map{ case (k,v) => v.title.getOrElse("no Title") -> v.`type` }

}

case class JSONSchemaL2(
                         `type`:String,
                         title:Option[String],
                         properties: Option[Map[String,JSONSchemaL3]],
                         required: Option[Seq[String]],
                         readonly: Option[Boolean],
                         enum: Option[Seq[String]],
                         order: Option[Int]
                       )

case class JSONSchemaL3(
                         `type`:String,
                         title:Option[String],
                         required: Option[Seq[String]],
                         readonly: Option[Boolean],
                         enum: Option[Seq[String]],
                         order: Option[Int]
                       )


object JSONSchema{
  def empty = JSONSchema("object",None,Map(),None,None,None,None)
}
