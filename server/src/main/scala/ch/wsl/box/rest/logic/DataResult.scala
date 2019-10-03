package ch.wsl.box.rest.logic

import io.circe._
import io.circe.syntax._

sealed trait DataResult

case class DataResultTable(headers:Seq[String],rows:Seq[Seq[String]]) extends DataResult {
  def json = {
    rows.map(_.zip(headers).toMap).asJson
  }
}
case class DataResultObject(obj:Json) extends DataResult
