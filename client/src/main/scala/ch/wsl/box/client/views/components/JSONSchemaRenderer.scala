package ch.wsl.box.client.views.components

import ch.wsl.box.model.shared.JSONSchema
import org.scalajs.dom.Element

import scalatags.JsDom.TypedTag


/**
  * Created by andre on 4/25/2017.
  */
object JSONSchemaRenderer {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  def apply(schema:JSONSchema):TypedTag[Element] = {

    div(
      for((name,props) <- schema.properties.toSeq) yield {
        div(name)
      }
    )
  }

  def apply(schema:Option[JSONSchema]):TypedTag[Element] = apply(schema.getOrElse(JSONSchema.empty))
}
