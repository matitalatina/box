package ch.wsl.box.model.shared

import java.text.SimpleDateFormat

import ch.wsl.box.shared.utils.DateTimeFormatters
import kantan.csv._
import kantan.csv.ops._



/**
  * Created by andreaminetti on 03/03/16.
  */
case class JSONData[M <: Product](data:Seq[M], count:Int) {

  import JSONData._
  def csv:String = data.map(_.values()).asCsv(rfc)
}

object JSONData{
  implicit class CSVWrapper(val prod: Product) extends AnyVal {

    def values():Seq[String] = prod.productIterator.map{
      case Some(value) => customToStringIfDateTime(value)
      case None => ""
      case value => customToStringIfDateTime(value)
    }.toSeq
  }

  def customToStringIfDateTime(v: Any) = v match {                          //here we can set the timestamp format for the generated tables' forms
    case x:java.time.LocalDateTime =>  DateTimeFormatters.timestamp.format(x)
    case x:java.time.LocalDate =>  DateTimeFormatters.date.format(x)
    case x:java.time.LocalTime =>  DateTimeFormatters.time.format(x)
    case _ => v.toString
  }

}