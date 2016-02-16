package ch.wsl.rest.service

import net.liftweb.json.DefaultFormats
import spray.httpx.LiftJsonSupport

/**
 * Created by andreaminetti on 16/02/16.
 */
object JsonProtocol extends LiftJsonSupport {
  implicit def liftJsonFormats = DefaultFormats
}