package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 29/02/16.
  */

case class JSONSchemaUI(
                         `ui:order`:Seq[String],
                         widgets: Map[String,UIWidget]
                       )

object JSONSchemaUI{
  def fromJSONFields(fields:Seq[JSONField]):JSONSchemaUI = JSONSchemaUI(
    `ui:order` = fields.map(_.key),
     widgets = fields.flatMap{ f => f.widget.map { w => f.key -> UIWidget(w) }}.toMap
  )

  def empty = JSONSchemaUI(Vector(),Map())
}

case class UIWidget(`ui:widget`: String)