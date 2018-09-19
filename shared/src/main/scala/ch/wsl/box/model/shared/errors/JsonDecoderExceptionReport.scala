package ch.wsl.box.model.shared.errors

import io.circe.Json

case class JsonDecoderExceptionReport(
  fields:Seq[String],
  cursors:Seq[String],
  message:String,
  original:Option[Json],
  source:String = "json"
) extends ExceptionReport {

  override def humanReadable(labels: Map[String, String]) = s"$message ${fields.mkString("for labels:",",","")}"

}
