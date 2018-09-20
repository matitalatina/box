package ch.wsl.box.rest.utils

import java.sql.{Time, Timestamp}
import java.text.SimpleDateFormat

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypeRange, HttpEntity}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import io.circe.Decoder.Result

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by andreaminetti on 12/10/16.
  *
  * this contains the serializer between the JSON and the Scala objects  (in the server)
  *
  */
object JSONSupport {


  private def jsonContentTypes: List[ContentTypeRange] =
    List(`application/json`)

  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    Unmarshaller.stringUnmarshaller
      .forContentTypes(jsonContentTypes: _*)
      .flatMap { ctx => mat => json =>
        decode[A](json).fold(Future.failed, Future.successful)
      }
  }

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] = {
    Marshaller.withFixedContentType(`application/json`) { a =>
      HttpEntity(`application/json`, a.asJson.noSpaces)
    }
  }

  implicit def printer: Json => String = Printer.noSpaces.copy(dropNullValues = true).pretty


  implicit val DateFormat : Encoder[java.sql.Date] with Decoder[java.sql.Date] = new Encoder[java.sql.Date] with Decoder[java.sql.Date] {

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    override def apply(a: java.sql.Date): Json = {
      Encoder.encodeString.apply(dateFormatter.format(a))
    }

    override def apply(c: HCursor): Result[java.sql.Date] = {
      Decoder.decodeString.map(s => new java.sql.Date(dateFormatter.parse(s).getTime)).apply(c)
    }

  }

  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {

    val timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")  //attention the format is different to that in the client for datetimepicker
    val timestampFormatterMin = new SimpleDateFormat("yyyy-MM-dd HH:mm")  //attention the format is different to that in the client for datetimepicker

    override def apply(a: Timestamp): Json = {
      Try {
        Encoder.encodeString.apply(timestampFormatter.format(a))
      }.getOrElse(Json.Null)
    }

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map{s =>
      val timestamp = Try{timestampFormatter.parse(s).getTime}.getOrElse(timestampFormatterMin.parse(s).getTime)
      new Timestamp(timestamp)
    }.apply(c)
  }


  implicit val TimeFormat : Encoder[Time] with Decoder[Time] = new Encoder[Time] with Decoder[Time] {

    val timeFormatter = new SimpleDateFormat("HH:mm:ss.S")

    override def apply(a: Time): Json = Encoder.encodeString.apply(timeFormatter.format(a))

    override def apply(c: HCursor): Result[Time] = Decoder.decodeString.map(s => new Time(timeFormatter.parse(s).getTime)).apply(c)
  }

}
