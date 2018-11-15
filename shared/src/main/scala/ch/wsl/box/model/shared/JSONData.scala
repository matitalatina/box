package ch.wsl.box.model.shared

import java.text.SimpleDateFormat

import ch.wsl.box.shared.utils.DateTimeFormatters
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}



/**
  * Created by andreaminetti on 03/03/16.
  */
case class JSONData[M <: Product](data:Seq[M], count:Int) {

  import JSONData._
  def csv:String = CSV.writeAll(data.map(_.values()))
}

object JSONData{
  implicit class CSVWrapper(val prod: Product) extends AnyVal {

    def values():Seq[String] = prod.productIterator.map{
      case Some(value) => customToStringIfDateTime(value)
      case None => ""
      case rest => rest.toString
    }.toSeq
  }

  def customToStringIfDateTime(v: Any) = v match {                          //here we can set the timestamp format for the generated tables' forms
    case x:java.sql.Timestamp =>  DateTimeFormatters.timestamp.format(x)  //todo: solve how to specify if the timestamp should be rendered as date, time or datetime and to which resolution
    case x:java.sql.Date =>  DateTimeFormatters.date.format(x)
    case x:java.sql.Time =>  DateTimeFormatters.time.format(x)
    case _ => v.toString
  }

}