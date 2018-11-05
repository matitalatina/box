package ch.wsl.box.model.shared.errors

trait ExceptionReport {
  /**
    *
    * @return Source of the error (i.e. JSON parsing, SQL insert)
    */
  def source:String

  def humanReadable(labels:Map[String,String]):String
}
