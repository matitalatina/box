package ch.wsl.box.rest.service

import java.sql.Date

import org.json4s.JsonAST.{JNull, JString}
import org.json4s.{CustomSerializer, Formats, DefaultFormats}
import spray.httpx.{Json4sSupport, LiftJsonSupport}
import ch.wsl.box.model.tables._


/**
 * Created by andreaminetti on 16/02/16.
 */
object JsonProtocol extends Json4sSupport {

  case object DateSerializer extends CustomSerializer[java.sql.Date](format => (
    {
      case JString(s) => Date.valueOf(s)
      case JNull => null
    },
    {
      case d: Date => JString(d.toString())
    }
    )
  )

  implicit def json4sFormats: Formats = DefaultFormats + DateSerializer
}
