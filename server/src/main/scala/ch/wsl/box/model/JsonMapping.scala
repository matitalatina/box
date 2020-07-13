package ch.wsl.box.model

import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

import ch.wsl.box.jdbc.PostgresProfile.api._



object JsonMapping {
  type JsonMapped = Json

  implicit val jsonMappedString = MappedColumnType.base[String,JsonMapped](
    str => str.asJson,
    json => json.as[String].right.get
  )

  implicit val jsonMappedInt = MappedColumnType.base[Int,JsonMapped](
    i => i.asJson,
    json => json.as[Int].right.get
  )

}
