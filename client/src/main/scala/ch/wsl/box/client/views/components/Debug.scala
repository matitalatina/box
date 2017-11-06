package ch.wsl.box.client.views.components

import io.udash.properties.model.ModelProperty

import scalatags.JsDom.all._
import io.udash._
import io.circe.syntax._
import org.scalajs.dom.Event

object Debug {

  import ch.wsl.box.client.Context._

  def apply[T](model: Property[T])(implicit enc:io.circe.Encoder[T]) = {
    val show = Property{false}

    val out = model.transform{ m =>
      m.asJson.spaces2
    }
    div(
      a(""),
      produce(show) { s =>
        s match {
          case true => div(
            a("Nascondi debug",onclick :+= ((e:Event) => show.set(false))),
            pre(bind(out))
          ).render
          case false => a("Mostra debug",onclick :+= ((e:Event) => show.set(true))).render
        }

      }
    )
  }
}
