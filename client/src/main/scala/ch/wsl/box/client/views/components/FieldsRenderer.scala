package ch.wsl.box.client.views.components

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import org.scalajs.dom.Element

import scalatags.JsDom.TypedTag

/**
  * Created by andre on 5/2/2017.
  */
object FieldsRenderer {

  import io.circe.syntax._

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  def apply(value:Json, field:JSONField):TypedTag[Element] = {
    val rawValue:String = value.hcursor.get[Json](field.key).fold(
      {x => println(x); ""},
      {x => x.as[String].right.getOrElse(x.toString())}
    )
    field.options match {
      case Some(opts) => {
        val label: String = opts.options.lift(rawValue).getOrElse("")
        a(href := ModelFormState(field.table,Some(rawValue)).url,label)
      }
      case None => p(rawValue)
    }
  }
}
