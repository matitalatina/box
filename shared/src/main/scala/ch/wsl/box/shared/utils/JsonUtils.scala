package ch.wsl.box.shared.utils

import ch.wsl.box.model.shared.JSONID
import io.circe._
import scribe.Logging

/**
  * Created by andre on 5/22/2017.
  */
object JsonUtils extends Logging {
  implicit class EnhancedJson(el:Json) {

    def string:String = {
      val result = el.fold(
        "",
        bool => bool.toString,
        num => num.toString,
        str => str,
        arr => arr.toString,
        obj => obj.toString
      )
      result
    }

    //return JSON value of the given field
    def js(field:String):Json = el.hcursor.get[Json](field).right.getOrElse(Json.Null)

    def seq(field:String):Seq[Json] = {
      val result = el.hcursor.get[Seq[Json]](field)
      logger.info(s"getting seq of $field, result: $result")
      result.right.getOrElse(Seq())
    }

    def get(field: String):String = getOpt(field).getOrElse("")

    def getOpt(field: String):Option[String] = el.hcursor.get[Json](field).fold(
      { _ =>
        None
      }, { x => Some(x.string) }
    )

    def ID(fields:Seq[String]):JSONID = {
      val values = fields map { field =>
        field -> get(field)
      }
      JSONID.fromMap(values.toMap)
    }
  }
}
