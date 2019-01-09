package ch.wsl.box.model.shared.errors

case class SQLExceptionReport(
                               schema:Option[String],
                               table:Option[String],
                               column:Option[String],
                               constraint:Option[String],
                               detail:Option[String],
                               hint:Option[String],
                               internalQuery:Option[String],
                               message:Option[String],
                               source:String = "sql"
                             ) extends ExceptionReport {
  override def humanReadable(labels: Map[String, String]) = s"Error in table ${table.getOrElse("Not defined")} ${
    column match{
      case Some(x) => s"(column $column)"
      case None =>""}
  }: ${message.getOrElse("no error message")}</br>[query: ${internalQuery.getOrElse("")}]</br>[hint:${hint.getOrElse("")}]</br>[detail:${detail.getOrElse("")}]"
}
