package ch.wsl.box.rest.utils

import java.sql.Timestamp
import java.time.temporal.ChronoUnit

import ch.wsl.box.model.shared.JSONFieldTypes

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try

import slick.jdbc.PostgresProfile.api._



object BoxConf {

  private var conf: Map[String, String] = Map()

  def load()(implicit ec: ExecutionContext) = {

    val query = for {
      row <- ch.wsl.box.rest.boxentities.Conf.table
    } yield row

    conf = Await.result(Auth.boxDB.run(query.result).map {
      _.map { row =>
        row.key -> row.value.getOrElse("")
      }.toMap
    }, 200 seconds)

  }

//  def pageLength = Try(conf("page_length").toInt).getOrElse(30)
//
//  def lookupMaxRows = Try(conf("fk_rows").toInt).getOrElse(30)
//
//  def manualEditKeyFields = Try(conf("manual_edit.key_fields").toBoolean).getOrElse(false)

  def filterEqualityPrecisionDatetime = Try(conf("filterEqualityPrecision.datetime").toUpperCase).toOption match {
    case Some("DATE") => JSONFieldTypes.DATE
    case Some("DATETIME") => JSONFieldTypes.DATETIME
    case _ => JSONFieldTypes.DATETIME //for None or wrong values
  }

  def prepareDatetime = filterEqualityPrecisionDatetime match {
    case JSONFieldTypes.DATE => ((x: Timestamp) => Timestamp.valueOf(x.toLocalDateTime.truncatedTo(ChronoUnit.DAYS)))
    case JSONFieldTypes.DATETIME => ((x: Timestamp) => x)
    case _ => ((x: Timestamp) => x)
  }

  def filterEqualityPrecisionDouble: Option[Int] = Try(conf("filterEqualityPrecision.double").toInt).toOption

  def prepareDouble = filterEqualityPrecisionDouble match {
    case None => ((x: Double) => x)
    case Some(p) => ((x: Double) => roundAt(p)(x))
    //    case Some(p) if p<0 => ((x:Double) => roundAt(-p)(x))
  }


  def roundAt(p: Int)(n: Double): Double = { val s = math.pow (10, p); (math.round(n) * s) / s }
  def truncateAt(p: Int)(n: Double): Double = { val s = math.pow (10, p); (math.floor(n) * s) / s }
}
