package ch.wsl.box.client.views.components

import ch.wsl.box.client.utils.UI
import io.udash.properties.model.ModelProperty

import scalatags.JsDom.all._
import io.udash._
import io.circe.syntax._
import org.scalajs.dom.Event

object Debug {

  import ch.wsl.box.client.Context._

  def apply[T](model: Property[T], name:String ="")(implicit enc:io.circe.Encoder[T]) = {
    val show = Property {
      false
    }

    val out = model.transform { m =>
      m.asJson.spaces2
    }
    if (UI.debug) {
      div(
        a(""),
        produce(show) {
            case true => div(
              a("Hide debug " + name, onclick :+= ((e: Event) => show.set(false))),
              pre(bind(out))
            ).render
            case false => a("Show debug " + name, onclick :+= ((e: Event) => show.set(true))).render
        }
      )
    } else frag()
  }
}
