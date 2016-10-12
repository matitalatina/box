package ch.wsl.box.rest.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe._
import io.circe.syntax._

/**
  * Created by andreaminetti on 12/10/16.
  */
object JsonImplicits {
  implicit val sqlDateEncoder: Encoder[java.sql.Date] = Encoder.instance(a => a.getTime.asJson)
  implicit val sqlDateDecoder: Decoder[java.sql.Date] = Decoder.instance(a => a.as[Long].map(new java.sql.Date(_)))
}
