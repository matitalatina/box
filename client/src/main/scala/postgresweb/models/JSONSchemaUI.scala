package postgresweb.models

/**
  * Created by andreaminetti on 29/02/16.
  */
case class JSONSchemaUI(`ui:order`:Seq[String])

object JSONSchemaUI{
  def fromJSONFields(fields:Seq[JSONField]):JSONSchemaUI = JSONSchemaUI(
    `ui:order` = fields.map(_.key)
  )

  def empty = JSONSchemaUI(Vector())
}
