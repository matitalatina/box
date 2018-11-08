package ch.wsl.box.rest.logic

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.{ChronoField, ChronoUnit, TemporalAccessor}

import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.{Filter, JSONFieldTypes, JSONQuery, JSONQueryFilter}
import ch.wsl.box.rest.utils.{BoxConf, DateTimeFormatters}
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
  def dislike(c:Col,v:String):Rep[Option[Boolean]]
  def in(c:Col,v:String):Rep[Option[Boolean]]
  def notin(c:Col,v:String):Rep[Option[Boolean]]
  def between(c:Col,v:String):Rep[Option[Boolean]]

  def operator(op:String)(c:Col,q:JSONQueryFilter) ={


    op match{
      case Filter.EQUALS  => ==(c, q.value)
      case Filter.NOT     => not(c, q.value)
      case Filter.>       => >(c, q.value)
      case Filter.<       => <(c, q.value)
      case Filter.>=      => >=(c, q.value)
      case Filter.<=      => <=(c, q.value)
      case Filter.LIKE    => like(c, q.value)
      case Filter.DISLIKE => dislike(c, q.value)
      case Filter.IN      => in(c, q.value)
      case Filter.NOTIN   => notin(c, q.value)
      case Filter.BETWEEN => between(c, q.value)
    }
  }

}

trait UglyDBFilters extends DbFilters with Logging {


  val toTimestamp = DateTimeFormatters.timestamp.parse _

  final val typINT = 0
  final val typLONG = 1
  final val typSHORT =  2
  final val typDOUBLE = 3
  final val typBOOLEAN = 4
  final val typSTRING = 5
  final val typTIMESTAMP = 6
  final val typOptINT = 10
  final val typOptLONG = 11
  final val typOptSHORT =  12
  final val typOptDOUBLE = 13
  final val typOptBOOLEAN = 14
  final val typOptSTRING = 15
  final val typOptTIMESTAMP = 16
  final val typError = 100


  def typ(myType:String):Int = myType match{
    case "scala.Short" | "Short" => typSHORT
    case "Double" | "scala.Double" => typDOUBLE
    case "scala.Int" | "java.lang.Integer" | "Int" => typINT
    case "scala.Long" | "Long" => typLONG
    case "String" => typSTRING
    case "Boolean" | "scala.Boolean" => typBOOLEAN
    case "java.sql.Timestamp" => typTIMESTAMP
    case "scala.Option[scala.Short]" | "Option[Short]" =>  typOptSHORT
    case "scala.Option[scala.Double]" | "Option[Double]" => typOptDOUBLE
    case "scala.Option[scala.Int]" | "scala.Option[java.lang.Integer]" | "Option[Int]" | "Option[java.lang.Integer]" => typOptINT
    case "scala.Option[scala.Long]"  | "Option[Long]" => typOptLONG
    case "scala.Option[String]" | "Option[String]" => typOptSTRING
    case "scala.Option[scala.Boolean]" | "Option[Boolean]" => typOptBOOLEAN
    case "scala.Option[java.sql.Timestamp]" | "Option[java.sql.Timestamp]" => typOptTIMESTAMP
    case _ => {
      logger.error("Type mapping for: " + myType + " not found")
      typError
    }
  }


  def ==(col:Col,value:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

      val v = value.toString
      val c:Rep[_] = col.rep



      typ(col.`type`) match {
          case `typSHORT` => c.asInstanceOf[Rep[Short]] === v.toShort
          case `typDOUBLE` => c.asInstanceOf[Rep[Double]] === v.toDouble
          case `typINT` => c.asInstanceOf[Rep[Int]] === v.toInt
          case `typLONG` => c.asInstanceOf[Rep[Long]] === v.toLong
          case `typSTRING` => c.asInstanceOf[Rep[String]] === v
          case `typBOOLEAN` => c.asInstanceOf[Rep[Boolean]] === v.toBoolean
          case `typTIMESTAMP` => BoxConf.filterEqualityPrecisionDatetime match{
            case JSONFieldTypes.DATETIME => c.asInstanceOf[Rep[Timestamp]] === toTimestamp(v).get
            case JSONFieldTypes.DATE => {c.asInstanceOf[Rep[Timestamp]] >= BoxConf.prepareDatetime(toTimestamp(v).get) &&
                                         c.asInstanceOf[Rep[Timestamp]] < BoxConf.prepareDatetime(Timestamp.from(toTimestamp(v).get.toInstant.plus(1, ChronoUnit.DAYS))) }
          }
          case `typOptSHORT` =>  c.asInstanceOf[Rep[Option[Short]]] === v.toShort
          case `typOptDOUBLE` => c.asInstanceOf[Rep[Option[Double]]] === v.toDouble
          case `typOptINT` => c.asInstanceOf[Rep[Option[Int]]] === v.toInt
          case `typOptLONG` => c.asInstanceOf[Rep[Option[Long]]] === v.toLong
          case `typOptSTRING` => c.asInstanceOf[Rep[Option[String]]] === v
          case `typOptBOOLEAN` => c.asInstanceOf[Rep[Option[Boolean]]] === v.toBoolean
          case `typOptTIMESTAMP` => c.asInstanceOf[Rep[Option[Timestamp]]] === toTimestamp(v).get
          case `typError`  => None
          case _ => None
      }
   }

  def not(col:Col,v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


    val c:Rep[_] = col.rep

    typ(col.`type`) match {
          case `typSHORT` => c.asInstanceOf[Rep[Short]] =!= v.toShort
          case `typINT` => c.asInstanceOf[Rep[Int]] =!= v.toInt
          case `typLONG` => c.asInstanceOf[Rep[Long]] =!= v.toLong
          case `typDOUBLE` => c.asInstanceOf[Rep[Double]] =!= v.toDouble
          case `typSTRING` => c.asInstanceOf[Rep[String]] =!= v
          case `typBOOLEAN` => c.asInstanceOf[Rep[Boolean]] =!= v.toBoolean
          case `typTIMESTAMP` => BoxConf.filterEqualityPrecisionDatetime match{
            case JSONFieldTypes.DATETIME => c.asInstanceOf[Rep[Timestamp]] =!= toTimestamp(v).get
            case JSONFieldTypes.DATE => {c.asInstanceOf[Rep[Timestamp]] < BoxConf.prepareDatetime(toTimestamp(v).get) ||
              c.asInstanceOf[Rep[Timestamp]] >= BoxConf.prepareDatetime(Timestamp.from(toTimestamp(v).get.toInstant.plus(1, ChronoUnit.DAYS))) }
          }
          case `typOptSHORT` => c.asInstanceOf[Rep[Option[Short]]] =!= v.toShort
          case `typOptDOUBLE` => c.asInstanceOf[Rep[Option[Double]]] =!= v.toDouble
          case `typOptINT` => c.asInstanceOf[Rep[Option[Int]]] =!= v.toInt
          case `typOptLONG` => c.asInstanceOf[Rep[Option[Long]]] =!= v.toLong
          case `typOptSTRING` => c.asInstanceOf[Rep[Option[String]]] =!= v
          case `typOptBOOLEAN` => c.asInstanceOf[Rep[Option[Boolean]]] =!= v.toBoolean
          case `typOptTIMESTAMP` => c.asInstanceOf[Rep[Option[Timestamp]]] =!= toTimestamp(v).get
          case `typError` => None
          case _ => None
      }
   }

  def >(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    typ(col.`type`) match {
          case `typSHORT` => c.asInstanceOf[Rep[Short]] > v.toShort
          case `typDOUBLE` => c.asInstanceOf[Rep[Double]] > v.toDouble
          case `typINT` => c.asInstanceOf[Rep[Int]] > v.toInt
          case `typLONG` => c.asInstanceOf[Rep[Long]] > v.toLong
          case `typTIMESTAMP` => BoxConf.filterEqualityPrecisionDatetime match{
            case JSONFieldTypes.DATETIME => c.asInstanceOf[Rep[Timestamp]] > toTimestamp(v).get
            case JSONFieldTypes.DATE => c.asInstanceOf[Rep[Timestamp]] >= BoxConf.prepareDatetime(Timestamp.from(toTimestamp(v).get.toInstant.plus(1, ChronoUnit.DAYS)))
          }
          case `typOptSHORT` => c.asInstanceOf[Rep[Option[Short]]] > v.toShort
          case `typOptDOUBLE` => c.asInstanceOf[Rep[Option[Double]]] > v.toDouble
          case `typOptINT` => c.asInstanceOf[Rep[Option[Int]]] > v.toInt
          case `typOptLONG` => c.asInstanceOf[Rep[Option[Long]]] > v.toLong
          case `typOptTIMESTAMP` => c.asInstanceOf[Rep[Option[Timestamp]]] > toTimestamp(v).get
          case `typError` => None
          case _ => None
      }
   }
   def >=(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

     typ(col.`type`) match {
          case `typSHORT` => c.asInstanceOf[Rep[Short]] >= v.toShort
          case `typDOUBLE` => c.asInstanceOf[Rep[Double]] >= v.toDouble
          case `typINT` => c.asInstanceOf[Rep[Int]] >= v.toInt
          case `typLONG` => c.asInstanceOf[Rep[Long]] >= v.toLong
          case `typTIMESTAMP` => BoxConf.filterEqualityPrecisionDatetime match{
            case JSONFieldTypes.DATETIME => c.asInstanceOf[Rep[Timestamp]] >= toTimestamp(v).get
            case JSONFieldTypes.DATE => c.asInstanceOf[Rep[Timestamp]] >= BoxConf.prepareDatetime(toTimestamp(v).get)
          }
          case `typOptSHORT` => c.asInstanceOf[Rep[Option[Short]]] >= v.toShort
          case `typOptDOUBLE` => c.asInstanceOf[Rep[Option[Double]]] >= v.toDouble
          case `typOptINT` => c.asInstanceOf[Rep[Option[Int]]] >= v.toInt
          case `typOptLONG` => c.asInstanceOf[Rep[Option[Long]]] >= v.toLong
          case `typOptTIMESTAMP` => c.asInstanceOf[Rep[Option[Timestamp]]] >= toTimestamp(v).get
          case `typError` => None
          case _ => None
      }
   }

  def <(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    typ(col.`type`) match {
          case `typSHORT` => c.asInstanceOf[Rep[Short]] < v.toShort
          case `typDOUBLE` => c.asInstanceOf[Rep[Double]] < v.toDouble
          case `typINT` => c.asInstanceOf[Rep[Int]] < v.toInt
          case `typLONG` => c.asInstanceOf[Rep[Long]] < v.toLong
          case `typTIMESTAMP` => BoxConf.filterEqualityPrecisionDatetime match{
            case JSONFieldTypes.DATETIME => c.asInstanceOf[Rep[Timestamp]] < toTimestamp(v).get
            case JSONFieldTypes.DATE => c.asInstanceOf[Rep[Timestamp]] < BoxConf.prepareDatetime(toTimestamp(v).get)
          }
          case `typOptSHORT` => c.asInstanceOf[Rep[Option[Short]]] < v.toShort
          case `typOptDOUBLE` => c.asInstanceOf[Rep[Option[Double]]] < v.toDouble
          case `typOptINT` => c.asInstanceOf[Rep[Option[Int]]] < v.toInt
          case `typOptLONG` => c.asInstanceOf[Rep[Option[Long]]] < v.toLong
          case `typOptTIMESTAMP` => c.asInstanceOf[Rep[Option[Timestamp]]] < toTimestamp(v).get
          case `typError` => None
          case _ => None
      }
   }

  def <=(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    typ(col.`type`) match {
          case `typSHORT` => c.asInstanceOf[Rep[Short]] <= v.toShort
          case `typDOUBLE` => c.asInstanceOf[Rep[Double]] <= v.toDouble
          case `typINT` => c.asInstanceOf[Rep[Int]] <= v.toInt
          case `typLONG` => c.asInstanceOf[Rep[Long]] <= v.toLong
          case `typTIMESTAMP` => BoxConf.filterEqualityPrecisionDatetime match{
            case JSONFieldTypes.DATETIME => c.asInstanceOf[Rep[Timestamp]] <= toTimestamp(v).get
            case JSONFieldTypes.DATE => c.asInstanceOf[Rep[Timestamp]] <= BoxConf.prepareDatetime(Timestamp.from(toTimestamp(v).get.toInstant.plus(1, ChronoUnit.DAYS)))
          }
          case `typOptSHORT` => c.asInstanceOf[Rep[Option[Short]]] <= v.toShort
          case `typOptDOUBLE` => c.asInstanceOf[Rep[Option[Double]]] <= v.toDouble
          case `typOptINT` => c.asInstanceOf[Rep[Option[Int]]] <= v.toInt
          case `typOptLONG` => c.asInstanceOf[Rep[Option[Long]]] <= v.toLong
          case `typOptTIMESTAMP` => c.asInstanceOf[Rep[Option[Timestamp]]] <= toTimestamp(v).get
          case `typError` => None
          case _ => None
      }
   }

  def like(col:Col,v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    logger.info("Executing like on" + col.toString)

    typ(col.`type`) match {
          case `typSTRING` => c.asInstanceOf[Rep[String]].toLowerCase like "%"+v.toLowerCase+"%"
          case `typOptSTRING` => c.asInstanceOf[Rep[Option[String]]].toLowerCase like "%"+v.toLowerCase+"%"
          case `typError` => None
          case _ => None
      }
   }

  def dislike(col:Col,v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    logger.info("Executing like on" + col.toString)

    typ(col.`type`) match {
      case `typSTRING` =>( !(c.asInstanceOf[Rep[String]].toLowerCase like "%"+v.toLowerCase+"%") || (c.asInstanceOf[Rep[String]]).length == 0)
      case `typOptSTRING` => (!(c.asInstanceOf[Rep[Option[String]]].toLowerCase like "%"+v.toLowerCase+"%") || c.asInstanceOf[Rep[Option[String]]].isEmpty)
      case `typError` => None
      case _ => None
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


  def in(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val elements = v.replace("  ", ",").
                      replace(" ", ",").
                      replace("+", ",").
                      replace("-", ",").
                      replace(" and ", ",").split(',').toSeq

    if (elements.size > 0 && elements.head.nonEmpty) {

      //      val reps: Seq[Rep[Boolean]] = elements.map(x => ==(col, x).getOrElse(false))
      val reps: Seq[Rep[Option[Boolean]]] = elements.map(x => this.==(col, x))

      //        def mergeReps = (x:Rep[Option[Boolean]],y:Rep[Option[Boolean]]) => x || y  //need to be either true

      //      reps.fold[Rep[Option[Boolean]]](Some(true))((x,y) => x && y)

      reps.reduceLeft[Rep[Option[Boolean]]]((x, y) => x || y)

    } else {
      None
    }

  }

  def notin(col:Col, v:String):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val elements = v.replace("  ", ",").
                      replace(" ", ",").
                      replace("+", ",").
                      replace("-", ",").
                      replace(" and ", ",").split(',').toSeq

    if (elements.size >0 && elements.head.nonEmpty) {

      //      val reps: Seq[Rep[Boolean]] = elements.map(x => ==(col, x).getOrElse(false))
      val reps: Seq[Rep[Option[Boolean]]] = elements.map(x => this.not(col, x))

      //        def mergeReps = (x:Rep[Option[Boolean]],y:Rep[Option[Boolean]]) => x || y  //need to be either true

      //      reps.fold[Rep[Option[Boolean]]](Some(true))((x,y) => x && y)

      reps.reduceLeft[Rep[Option[Boolean]]]((x,y) => x && y)

    } else{
      None
    }

  }

}