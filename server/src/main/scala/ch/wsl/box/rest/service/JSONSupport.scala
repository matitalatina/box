package ch.wsl.box.rest.service

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.{Encoder, Json, Printer}
import akka.http.scaladsl.model.MediaTypes.`application/json`

/**
  * Created by andreaminetti on 12/10/16.
  */
object JSONSupport extends CirceSupport{
  implicit def printer: Json => String = Printer.noSpaces.copy(dropNullKeys = true).pretty
}
