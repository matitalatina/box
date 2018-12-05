package ch.wsl.box.shared.utils

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import java.time._

import scala.util.{Failure, Try}

trait DateTimeFormatters[T]{

  protected val formats:Seq[String]



  def format(dt:T,format:String = formats.last):String

  def parse(str:String):Option[T] = toLocalDateTime(str).map(fromLocalDateTime)

  //this is to format a timestamp with data only and add time to 00:00
  lazy val dateOnlyFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter();

  lazy val dateTimeFormats = formats.map(p => DateTimeFormatter.ofPattern(p)).+:(dateOnlyFormatter)


  def fromLocalDateTime(l:LocalDateTime):T

  def toLocalDateTime(dateStr: String): Option[LocalDateTime] = {


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
      normalize(dateTimeFormats).toOption
    }
  }
}

object DateTimeFormatters {


  val timestamp = new DateTimeFormatters[LocalDateTime] {
    override protected val formats: Seq[String] = Seq(
      "yyyy-MM-dd HH:mm:ss.S",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd HH:mm"
    )

    override def fromLocalDateTime(l:LocalDateTime): LocalDateTime = l


    override def format(dt: LocalDateTime, format: String): String = dt.format(DateTimeFormatter.ofPattern(format))
  }

  val date = new DateTimeFormatters[LocalDate] {
    override protected val formats: Seq[String] = Seq(
      "yyyy-MM-dd"
    )


    override def format(dt: LocalDate, format: String): String = dt.format(DateTimeFormatter.ofPattern(format))

    override def fromLocalDateTime(l:LocalDateTime): LocalDate = l.toLocalDate
  }

  val time = new DateTimeFormatters[LocalTime] {
    override protected val formats: Seq[String] = Seq(
      "HH:mm:ss.S",
      "HH:mm:ss",
      "HH:mm"
    )


    override def format(dt: LocalTime, format: String): String = dt.format(DateTimeFormatter.ofPattern(format))

    override def fromLocalDateTime(l:LocalDateTime): LocalTime = l.toLocalTime
  }


}
