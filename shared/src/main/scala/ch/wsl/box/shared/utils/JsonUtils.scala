package ch.wsl.box.shared.utils

import ch.wsl.box.model.shared.JSONKeys
import io.circe._

/**
  * Created by andre on 5/22/2017.
  */
object JsonUtils {
  implicit class EnhancedJson(el:Json) {
    def get(field: String) = el.hcursor.get[Json](field).fold(
      { x => "" }, { x => x.as[String].right.getOrElse(x.toString()) }
    )

    def keys(fields:Seq[String]) :JSONKeys = {
      val values = fields map { field =>
        get(field)
      }
      JSONKeys.fromMap(fields.zip(values).toMap)
    }
  }
}
