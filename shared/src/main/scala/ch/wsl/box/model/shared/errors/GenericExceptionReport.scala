package ch.wsl.box.model.shared.errors

case class GenericExceptionReport(body:String, source:String = "generic") extends ExceptionReport {
  override def humanReadable(labels: Map[String, String]) = s"$body"
}