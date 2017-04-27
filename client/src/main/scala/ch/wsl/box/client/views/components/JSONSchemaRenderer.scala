package ch.wsl.box.client.views.components

import ch.wsl.box.model.shared.JSONSchema
import io.udash.properties.single.Property
import org.scalajs.dom.Element
import io.udash._

import scalatags.JsDom.TypedTag


/**
  * Created by andre on 4/25/2017.
  */
object JSONSchemaRenderer {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  def apply(schema:JSONSchema,results: Seq[Property[String]]):TypedTag[Element] = {

    div(
      for(((name,props),i) <- schema.properties.toSeq.zipWithIndex) yield {
        div(
          b(name),
          br,
          results.lift(i).map { r =>
            TextInput(r)(placeholder := "Input your name...")
          }
        )
      }
    )
  }

  def apply(schema:Option[JSONSchema],results: Seq[Property[String]]):TypedTag[Element] = apply(schema.getOrElse(JSONSchema.empty),results)
}
