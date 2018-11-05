package ch.wsl.box.model.shared

import java.text.SimpleDateFormat

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
      case Some(value) => customToStringIfTimestamp(value)
      case None => ""
      case rest => rest.toString
    }.toSeq
  }

  def customToStringIfTimestamp(v: Any) = v match {                          //here we can set the timestamp format for the generated tables' forms
    case x:java.sql.Timestamp =>  new SimpleDateFormat("yyyy-MM-dd HH:mm").format(x)  //todo: to solve how to specify if the timestamp should be rendered as date, time or datetime and to which resolution
    case _ => v.toString
  }

}