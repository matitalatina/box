package ch.wsl.box.rest.utils

import java.sql.Timestamp
import java.time.temporal.ChronoUnit

import ch.wsl.box.model.shared.JSONFieldTypes
import com.typesafe.config.ConfigFactory
import scribe.{Level, Logger, Logging}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try
import slick.jdbc.PostgresProfile.api._



object BoxConf extends Logging {

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

  def clientConf:Map[String, String] = conf.filterKeys(Set(
              "host",
              "port",
              "cookie.name",
              "max-age",
              "logger.level",
              "langs",
              "manual_edit.key_fields",
              "rest.lookup.labels",
              "lookupMaxRows",
              "image_height",
              "filterEqualityPrecision.double",
              "filterEqualityPrecision.datetime",
              "display.index.html",
              "display.index.news"
            ))

  def restLookupLabels = ConfigFactory.parseString( Try(conf("rest.lookup.labels")).getOrElse("default=firstNoPKField"))

  def limitLookupFromFk = Try(conf("limitLookupFromFk").toInt).getOrElse(50)

  def akkaHttpSession = {
    val cookieName = Try(conf("cookie.name")).getOrElse("_boxsession")
    val maxAge = Try(conf("max-age")).getOrElse("2000")
    val serverSecret = Try(conf("server-secret")).getOrElse {
      logger.warn("Set server secret in application.conf table, use the default value only for development")
      "changeme530573985739845704357308s70487s08970897403854s038954s38754s30894387048s09e8u408su5389s5"
    }


    ConfigFactory.parseString(
      s"""akka.http.session {
         |  cookie {
         |    name = "$cookieName"
         |  }
         |  max-age = $maxAge seconds
         |  encrypt-data = true
         |  server-secret = "$serverSecret"
         |}""".stripMargin)

  }.withFallback(ConfigFactory.load())

  def host = Try(conf("host")).getOrElse("0.0.0.0")
  def port = Try(conf("port").toInt).getOrElse(8080)

  def loggerLevel = Try(conf("logger.level")).getOrElse("warn").toLowerCase match {
    case "trace" => Level.Trace
    case "debug" => Level.Debug
    case "info" => Level.Info
    case "warn" => Level.Warn
    case "error" => Level.Error
  }




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
