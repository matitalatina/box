package ch.wsl.box.shared.utils

import ch.wsl.box.model.shared.JSONID
import io.circe._
import scribe.Logging

/**
  * Created by andre on 5/22/2017.
  */
object JSONUtils extends Logging {

  val LANG = "::lang"
  val FIRST = "::first"

  implicit class EnhancedJson(el:Json) {


    def injectLang(lang:String):Json = el.isString match{
      case true =>  el.mapString(s => if (s.equals("::lang")) lang else s)
      case _ => el
    }


    def string:String = {
      val result = el.fold(
        "",
        bool => bool.toString,
        num => num.toString,
        str => str,
        arr => arr.map(_.string).mkString("[", ",", "]"),
        obj => obj.toString
      )
      result
    }

    //return JSON value of the given field
    def js(field:String):Json = el.hcursor.get[Json](field).right.getOrElse(Json.Null)

    def seq(field:String):Seq[Json] = {
      val result = el.hcursor.get[Seq[Json]](field)
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
