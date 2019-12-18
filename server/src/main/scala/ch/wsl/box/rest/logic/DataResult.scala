package ch.wsl.box.rest.logic

import io.circe._
import io.circe.syntax._

sealed trait DataResult

case class DataResultTable(headers:Seq[String],rows:Seq[Seq[String]]) extends DataResult {

  lazy val toMap: Seq[Map[String, String]] = rows.map(r => headers.zip(r).toMap)

  def json = {
    toMap.asJson
  }
}
case class DataResultObject(obj:Json) extends DataResult
