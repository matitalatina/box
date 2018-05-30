package ch.wsl.box.rest.logic

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.{Filter, JSONQuery, JSONQueryFilter}
import scribe.Logging
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

import scala.util.{Failure, Try}

trait DbFilters {
  def ==(c:Col,v:String):Rep[Option[Boolean]]
  def not(c:Col,v:String):Rep[Option[Boolean]]
  def >(c:Col, v:String):Rep[Option[Boolean]]
  def <(c:Col, v:String):Rep[Option[Boolean]]
  def >=(c:Col, v:String):Rep[Option[Boolean]]
  def <=(c:Col, v:String):Rep[Option[Boolean]]
  def like(c:Col,v:String):Rep[Option[Boolean]]
  def in(c:Col,v:String):Rep[Option[Boolean]]
  def between(c:Col,v:String):Rep[Option[Boolean]]
  def fkLike(c:Col,q:JSONQueryFilter):Rep[Option[Boolean]]
  def fkEquals(c:Col,q:JSONQueryFilter):Rep[Option[Boolean]]
  def fkNot(c:Col,q:JSONQueryFilter):Rep[Option[Boolean]]

  def operator(op:String)(c:Col,q:JSONQueryFilter) ={


    op match{
      case Filter.EQUALS  => ==(c, q.value)
      case Filter.NOT     => not(c, q.value)
      case Filter.>       => >(c, q.value)
      case Filter.<       => <(c, q.value)
      case Filter.>=      => >=(c, q.value)
      case Filter.<=      => <=(c, q.value)
      case Filter.LIKE    => like(c, q.value)
      case Filter.IN      => in(c, q.value)
      case Filter.BETWEEN   => between(c, q.value)
      case Filter.FK_LIKE   => fkLike(c, q)
      case Filter.FK_EQUALS   => fkEquals(c, q)
      case Filter.FK_NOT  => fkNot(c, q)
    }
  }

}

trait UglyDBFilters extends DbFilters with Logging {

  val timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")  //attention the format is different to that in the client for datetimepicker


  val dateTimeFormats = List(
    "yyyy-MM-dd HH:mm:ss.S",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm",
  ).map(p => DateTimeFormatter.ofPattern(p))


  def toTimestamp(dateStr: String): Option[Timestamp] = {
    val trimmedDate = dateStr.trim

    def normalize(patterns: Seq[DateTimeFormatter]): Try[LocalDateTime] = patterns match {
      case head::tail => {
        val resultTry = Try(LocalDateTime.parse(trimmedDate,head))
        if(resultTry.isSuccess) resultTry else normalize(tail)
      }
      case _ => Failure(new RuntimeException(s"no formatter match found for $dateStr"))
    }
    if(trimmedDate.isEmpty) None
    else {
      normalize(dateTimeFormats).toOption.map{ ldt =>
        Timestamp.from(ldt.toInstant(ZoneOffset.ofHours(0)))
      }
    }
  }



  def ==(col:Col,value:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

      val v = value.toString
      val c:Rep[_] = col.rep

      col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] === v.toShort
          case "Double" | "scala.Double" => c.asInstanceOf[Rep[Double]] === v.toDouble
          case "scala.Int" | "java.lang.Integer" | "Int" => c.asInstanceOf[Rep[Int]] === v.toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] === v.toLong
          case "String" => c.asInstanceOf[Rep[String]] === v
          case "scala.Boolean" => c.asInstanceOf[Rep[Boolean]] === v.toBoolean
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] === toTimestamp(v).get
          case "scala.Option[scala.Short]" =>  c.asInstanceOf[Rep[Option[Short]]] === v.toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] === v.toDouble
          case "scala.Option[scala.Int]" | "scala.Option[java.lang.Integer]" => c.asInstanceOf[Rep[Option[Int]]] === v.toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] === v.toLong
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] === v
          case "scala.Option[scala.Boolean]" => c.asInstanceOf[Rep[Option[Boolean]]] === v.toBoolean
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] === toTimestamp(v).get
          case _ => {
                logger.error("Type mapping for: " + col.`type`+ " not found")
                None
          }
      }
   }

  def not(col:Col,v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] =!= v.toShort
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] =!= v.toInt
          case "scala.Long" | "Int" => c.asInstanceOf[Rep[Long]] =!= v.toLong
          case "Double" | "scala.Double" => c.asInstanceOf[Rep[Double]] =!= v.toDouble
          case "String" => c.asInstanceOf[Rep[String]] =!= v
          case "scala.Boolean" => c.asInstanceOf[Rep[Boolean]] =!= v.toBoolean
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] =!= toTimestamp(v).get
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] =!= v.toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] =!= v.toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] =!= v.toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] =!= v.toLong
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] =!= v
          case "scala.Option[scala.Boolean]" => c.asInstanceOf[Rep[Option[Boolean]]] =!= v.toBoolean
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] =!= toTimestamp(v).get
          case _ => {
            logger.error("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def >(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] > v.toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] > v.toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] > v.toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] > v.toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] > toTimestamp(v).get
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] > v.toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] > v.toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] > v.toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] > v.toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] > toTimestamp(v).get
          case _ => {
              logger.error("Type mapping for: " + col.`type` + " not found")
              None
          }
      }
   }
   def >=(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] >= v.toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] >= v.toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] >= v.toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] >= v.toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] >= toTimestamp(v).get
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] >= v.toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] >= v.toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] >= v.toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] >= v.toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] >= toTimestamp(v).get
          case _ => {
              logger.error("Type mapping for: " + col.`type` + " not found")
              None
          }
      }
   }

  def <(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] < v.toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] < v.toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] < v.toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] < v.toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] < toTimestamp(v).get
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] < v.toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] < v.toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] < v.toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] < v.toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] < toTimestamp(v).get
          case _ => {
            logger.error("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def <=(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] <= v.toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] <= v.toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] <= v.toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] <= v.toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] <= toTimestamp(v).get
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] <= v.toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] <= v.toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] <= v.toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] <= v.toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] <= toTimestamp(v).get
          case _ => {
            logger.error("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def like(col:Col,v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    logger.info("Executing like on" + col.toString)

    col.`type` match {
          case "String" => c.asInstanceOf[Rep[String]].toLowerCase like "%"+v.toLowerCase+"%"
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]].toLowerCase like "%"+v.toLowerCase+"%"
          case _ => {
            logger.error("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def between(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val extremes = v.replace("to", "-").replace("and", "-").split('-')

    if (extremes.length == 2) {
      val lbound = >=(col, extremes(0).trim)//.getOrElse(false)
      val ubound = <=(col, extremes(1).trim)//.getOrElse(false)
      lbound && ubound //both need to be true
    } else {
      None
    }
  }

//  def in(col:Col, v:Any):Rep[Option[Boolean]] = ???
    def in(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

      val elements = v.split(',').toSeq

      if (elements.size >0) {

        //      val reps: Seq[Rep[Boolean]] = elements.map(x => ==(col, x).getOrElse(false))
        val reps: Seq[Rep[Option[Boolean]]] = elements.map(x => this.==(col, x))

//        def mergeReps = (x:Rep[Option[Boolean]],y:Rep[Option[Boolean]]) => x || y  //need to be either true

        //      reps.fold[Rep[Option[Boolean]]](Some(true))((x,y) => x && y)

        reps.reduceLeft[Rep[Option[Boolean]]]((x,y) => x || y)

      } else{
        None
      }

    }

  override def fkLike(c:Col,q:JSONQueryFilter):Rep[Option[Boolean]] = {
    q.lookup.get.lookup.filter(_._2.toLowerCase.contains(q.value.toLowerCase()))
      .foldRight[Rep[Option[Boolean]]](Some(false)) { case (el, cond) =>
      cond || ==(c,el._1)
    }

  }

  override def fkEquals(c: Col,q:JSONQueryFilter):Rep[Option[Boolean]] = {
    q.lookup.get.lookup.find(_._2 == q.value) match {
      case Some(v) => ==(c,v._1)
      case None => Some(false)
    }
  }


  override def fkNot(c: Col, q:JSONQueryFilter):Rep[Option[Boolean]] = {
    q.lookup.get.lookup.find(_._2 == q.value) match {
      case Some(v) => not(c,v._1)
      case None => Some(true)
    }
  }
}