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
import ch.wsl.box.shared.utils.DateTimeFormatters
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

  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {

    override def apply(a: Timestamp): Json = Try {
        Encoder.encodeString.apply(DateTimeFormatters.timestamp.format(a, BoxConf.dtFormatDatetime))
      }.getOrElse(Json.Null)


    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map{s =>
      DateTimeFormatters.timestamp.parse(s).get
    }.apply(c)
  }

  implicit val DateFormat : Encoder[java.sql.Date] with Decoder[java.sql.Date] = new Encoder[java.sql.Date] with Decoder[java.sql.Date] {

    override def apply(a: java.sql.Date): Json = Try {
      Encoder.encodeString.apply(DateTimeFormatters.date.format(a, BoxConf.dtFormatDate))
    }.getOrElse(Json.Null)

    override def apply(c: HCursor): Result[java.sql.Date] = Decoder.decodeString.map{s =>
      DateTimeFormatters.date.parse(s).get
    }.apply(c)

  }

  implicit val TimeFormat : Encoder[Time] with Decoder[Time] = new Encoder[Time] with Decoder[Time] {

    override def apply(a: Time): Json = Try {
      Encoder.encodeString.apply(DateTimeFormatters.time.format(a, BoxConf.dtFormatTime))
    }.getOrElse(Json.Null)


    override def apply(c: HCursor): Result[Time] = Decoder.decodeString.map{s =>
      DateTimeFormatters.time.parse(s).get
    }.apply(c)
  }

}
