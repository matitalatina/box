package ch.wsl.box.rest.service

import org.json4s.{Formats, DefaultFormats}
import spray.httpx.{Json4sSupport, LiftJsonSupport}
import ch.wsl.box.model.tables._


/**
 * Created by andreaminetti on 16/02/16.
 */
object JsonProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}
