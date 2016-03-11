package postgresweb.models

/**
  * Created by andreaminetti on 23/02/16.
  */
case class JSONSchema(
                       `type`:String,
                       title:Option[String] = None,
                       properties: Map[String,JSONSchemaL2] = Map(),
                       required: Option[Seq[String]] = None,
                       readonly: Option[Boolean] = None,
                       enum: Option[Seq[String]] = None,
                       order: Option[Int] = None
                     ) {

  def typeOfTitle:Map[String,String] = properties.map{ case (k,v) => v.title.getOrElse("no Title") -> v.`type` }

}

case class JSONSchemaL2(
                       `type`:String,
                       title:Option[String] = None,
                       properties: Option[Map[String,JSONSchemaL3]] = None,
                       required: Option[Seq[String]] = None,
                       readonly: Option[Boolean] = None,
                       enum: Option[Seq[String]] = None,
                       order: Option[Int] = None
                     )

case class JSONSchemaL3(
                       `type`:String,
                       title:Option[String] = None,
                       required: Option[Seq[String]] = None,
                       readonly: Option[Boolean] = None,
                       enum: Option[Seq[String]] = None,
                       order: Option[Int] = None
                     )

object JSONSchema{
  def empty = JSONSchema("object")
}