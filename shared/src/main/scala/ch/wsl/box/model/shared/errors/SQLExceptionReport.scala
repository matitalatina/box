package ch.wsl.box.model.shared.errors

case class SQLExceptionReport(
                               schema:String,
                               table:String,
                               column:String,
                               constraint:Option[String],
                               detail:Option[String],
                               hint:Option[String],
                               internalQuery:Option[String],
                               message:Option[String],
                               source:String = "sql"
                             ) extends ExceptionReport {
  override def humanReadable(labels: Map[String, String]) = s"Error in $table.$column $message"
}
