package ch.wsl.box.rest.service

import java.sql.Timestamp
import java.text.SimpleDateFormat

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import io.circe.Decoder.Result

/**
  * Created by andreaminetti on 12/10/16.
  */
object JSONSupport extends CirceSupport{
  implicit def printer: Json => String = Printer.noSpaces.copy(dropNullKeys = true).pretty


  implicit val DateFormat : Encoder[java.sql.Date] with Decoder[java.sql.Date] = new Encoder[java.sql.Date] with Decoder[java.sql.Date] {

    val dateFormatter = new SimpleDateFormat("yyyy-mm-dd")

    override def apply(a: java.sql.Date): Json = {
      Encoder.encodeString.apply(dateFormatter.format(a))
    }

    override def apply(c: HCursor): Result[java.sql.Date] = {
      Decoder.decodeString.map(s => new java.sql.Date(dateFormatter.parse(s).getTime)).apply(c)
    }

  }

  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {

    val timestampFormatter = new SimpleDateFormat("yyyy-mm-dd")

    override def apply(a: Timestamp): Json = Encoder.encodeString.apply(timestampFormatter.format(a))

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map(s => new Timestamp(timestampFormatter.parse(s).getTime)).apply(c)
  }

}
