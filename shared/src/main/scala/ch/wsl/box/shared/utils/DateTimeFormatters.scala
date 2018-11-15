package ch.wsl.box.shared.utils

import java.sql.{Date, Time, Timestamp}
import java.text.SimpleDateFormat
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import java.time.{LocalDateTime, ZoneOffset}

import scala.util.{Failure, Try}

trait DateTimeFormatters[T <: java.util.Date]{

  protected val formats:Seq[String]


  def format(dt:T,format:String = formats.last):String = new SimpleDateFormat(format).format(dt)

  def parse(str:String):Option[T] = toLong(str).map(fromLong)

  //this is to format a timestamp with data only and add time to 00:00
  lazy val dateOnlyFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter();

  lazy val dateTimeFormats = formats.map(p => DateTimeFormatter.ofPattern(p)).+:(dateOnlyFormatter)


  def fromLong(l:Long):T

  def toLong(dateStr: String): Option[Long] = {


    val trimmedDate = dateStr.trim

    def normalize(patterns: Seq[DateTimeFormatter]): Try[LocalDateTime] = patterns match {
      case head::tail => {
        val resultTry = Try(LocalDateTime.parse(trimmedDate, head))

        if(resultTry.isSuccess) resultTry else normalize(tail)
      }
      case _ => Failure(new RuntimeException(s"no formatter match found for $dateStr"))
    }

    if(trimmedDate.isEmpty)
      None
    else {
      normalize(dateTimeFormats).toOption.map{ ldt =>
        Timestamp.from(ldt.toInstant(ZoneOffset.ofHours(0))).getTime
      }
    }
  }
}

object DateTimeFormatters {


  val timestamp = new DateTimeFormatters[Timestamp] {
    override protected val formats: Seq[String] = Seq(
      "yyyy-MM-dd HH:mm:ss.S",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd HH:mm"
    )

    override def fromLong(l: Long): Timestamp = new Timestamp(l)
  }

  val date = new DateTimeFormatters[Date] {
    override protected val formats: Seq[String] = Seq(
      "yyyy-MM-dd"
    )

    override def fromLong(l: Long): Date = new Date(l)
  }

  val time = new DateTimeFormatters[Time] {
    override protected val formats: Seq[String] = Seq(
      "HH:mm:ss.S",
      "HH:mm:ss",
      "HH:mm"
    )

    override def fromLong(l: Long): Time = new Time(l)
  }


}
