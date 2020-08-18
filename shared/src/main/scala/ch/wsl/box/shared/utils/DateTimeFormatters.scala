package ch.wsl.box.shared.utils

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import java.time._

import scala.util.{Failure, Try}

trait DateTimeFormatters[T]{

  protected val parsers:Seq[String]
  protected val stringFormatter:String



  def format(dt:T,format:String = stringFormatter):String

  def parse(str:String):Option[T] = toObject(str)

  //this is to format a timestamp with data only and add time to 00:00
  lazy val dateOnlyFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter();

  lazy val dateTimeFormats = parsers.map(p => DateTimeFormatter.ofPattern(p))//.+:(dateOnlyFormatter)


  protected def parser(str:String,pattern:DateTimeFormatter):T

  private def toObject(dateStr: String): Option[T] = {


    val trimmedDate = dateStr.trim

    def normalize(patterns: Seq[DateTimeFormatter]): Try[T] = patterns match {
      case head::tail => {
        val resultTry = Try(parser(trimmedDate, head))


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
    override protected val parsers: Seq[String] = Seq(
      "yyyy-MM-dd HH:mm:ss.S",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd HH:mm",
      "yyyy-MM-dd",
      "yyyy-MM",
      "yyyy",
    )


    override protected val stringFormatter: String = "yyyy-MM-dd HH:mm"

    override def parser(str: String, pattern: DateTimeFormatter): LocalDateTime = LocalDateTime.parse(str,pattern)

    override def format(dt: LocalDateTime, format: String): String = dt.format(DateTimeFormatter.ofPattern(format))
  }

  val date = new DateTimeFormatters[LocalDate] {
    override protected val parsers: Seq[String] = Seq(
      "yyyy-MM-dd",
      "yyyy-MM",
      "yyyy",
    )


    override protected val stringFormatter: String = "yyyy-MM-dd"

    override def parser(str: String, pattern: DateTimeFormatter): LocalDate = LocalDate.parse(str,pattern)

    override def format(dt: LocalDate, format: String): String = dt.format(DateTimeFormatter.ofPattern(format))

  }

  val time = new DateTimeFormatters[LocalTime] {
    override protected val parsers: Seq[String] = Seq(
      "HH:mm:ss.S",
      "HH:mm:ss",
      "HH:mm"
    )


    override protected val stringFormatter: String = "HH:mm"

    override def parser(str: String, pattern: DateTimeFormatter): LocalTime = LocalTime.parse(str,pattern)


    override def format(dt: LocalTime, format: String): String = dt.format(DateTimeFormatter.ofPattern(format))

  }


}
