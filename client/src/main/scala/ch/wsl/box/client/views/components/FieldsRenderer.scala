package ch.wsl.box.client.views.components

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.model.shared.{JSONField, JSONKeys}
import io.circe.Json
import org.scalajs.dom.{Element, Event}
import io.udash._

import scalatags.JsDom.TypedTag

/**
  * Created by andre on 5/2/2017.
  */
object FieldsRenderer {

  import io.circe.syntax._

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  import ch.wsl.box.client.services.Enhancer._


  def toggleEdit(editing:Property[Boolean]) = {
    println("Toggle edit")
    editing.set(!editing.get)
  }

  def apply(value:String, field:JSONField, keys:JSONKeys):TypedTag[Element] = {


    val contentFixed = field.options match {
      case Some(opts) => {
        val label: String = opts.options.lift(value).getOrElse(value)
        val finalLabel = if(label.trim.length > 0) label else value
        a(href := ModelFormState(opts.refModel,Some(JSONKeys.fromMap(Map(field.key -> value)).asString)).url,finalLabel)
      }
      case None => p(value)
    }

    val editing = Property(false)
    val model = Property(value)

    div(onclick :+= ((ev: Event) => toggleEdit(editing), true),
      showIf(editing.transform(x => !x))(contentFixed.render),
      showIf(editing)(div(JSONSchemaRenderer.fieldRenderer(field,model,keys.keys.map(_.key),false)).render)
    )


  }
}
