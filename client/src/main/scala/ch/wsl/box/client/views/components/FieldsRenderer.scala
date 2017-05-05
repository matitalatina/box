package ch.wsl.box.client.views.components

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.model.shared.JSONField
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

  def apply(value:Json, field:JSONField, keys:Seq[String], model:Property[String], editing:Property[Boolean]):TypedTag[Element] = {
    val rawValue:String = value.hcursor.get[Json](field.key).fold(
      {x => println(x); ""},
      {x => x.as[String].right.getOrElse(x.toString())}
    )
    val contentFixed = field.options match {
      case Some(opts) => {
        val label: String = opts.options.lift(value.get(field.key)).getOrElse("")
        a(href := ModelFormState(field.table,Some(value.keys(keys).asString)).url,label)
      }
      case None => p(rawValue)
    }

    div(onclick :+= ((ev: Event) => toggleEdit(editing), true),
      showIf(editing.transform(x => !x))(contentFixed.render),
      showIf(editing)(div(JSONSchemaRenderer.fieldRenderer(field,model,false)).render)
    )

  }
}
