package ch.wsl.box.shared.utils

import ch.wsl.box.model.shared.JSONKeys
import io.circe._

/**
  * Created by andre on 5/22/2017.
  */
object JsonUtils {
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

    def js(field:String):Json = el.hcursor.get[Json](field).right.getOrElse(Json.Null)

    def seq(field:String):Seq[Json] = el.hcursor.get[Seq[Json]](field).right.getOrElse(Seq())

    def get(field: String) = el.hcursor.get[Json](field).fold(
      { x =>
        //println(s"error getting $field on $el: $x");
        ""
      }, { x => x.string }
    )

    def keys(fields:Seq[String]) :JSONKeys = {
      val values = fields map { field =>
        get(field)
      }
      JSONKeys.fromMap(fields.zip(values).toMap)
    }
  }
}
