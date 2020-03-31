package ch.wsl.box.client.utils

import java.sql.Timestamp
import java.time.temporal.ChronoUnit

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.{GlobalStyles, StyleConf}
import ch.wsl.box.client.styles.constants.StyleConstants.Colors
import ch.wsl.box.model.shared.JSONFieldTypes

import scala.util.Try

/**
  * Created by andre on 6/8/2017.
  */


object ClientConf {

  import ch.wsl.box.client.Context._

  private var conf:Map[String,String] = Map()

  def load() = REST.conf().map { table =>
    conf = table
  }

  def pageLength  = Try(conf("page_length").toInt).getOrElse(30)
//  def lookupMaxRows  = Try(conf("fk_rows").toInt).getOrElse(30)

  def manualEditKeyFields = Try(conf("pks.edit").toBoolean).getOrElse(false)
  def manualEditSingleKeyFields = Try(conf("pks.edit.single").trim().replace(' ',',').split(",").toSeq).getOrElse(Seq[String]())

  def displayIndexNews = Try(conf("display.index.news").toBoolean).getOrElse(false)
  def displayIndexHtml = Try(conf("display.index.html").toBoolean).getOrElse(false)


  def colorMain = Try(conf("color.main")).getOrElse("#006268")
  def colorLink = Try(conf("color.link")).getOrElse("#fbf0b2")
  def colorDanger = Try(conf("color.danger")).getOrElse("#4c1c24")
  def colorWarning = Try(conf("color.warning")).getOrElse("#ffa500")

  def tableFontSize = Try(conf("table.fontSize").toInt).getOrElse(10)

  lazy val style = GlobalStyles(StyleConf(colors = Colors(colorMain,colorLink,colorDanger,colorWarning), tableFontSize))


  def filterPrecisionDatetime = Try(conf("filter.precision.datetime").toUpperCase).toOption match {
      case Some("DATE") => JSONFieldTypes.DATE
      case Some("DATETIME") => JSONFieldTypes.DATETIME
      case _ => JSONFieldTypes.DATETIME     //for None or wrong values
    }

  def prepareDatetime = filterPrecisionDatetime match{
    case JSONFieldTypes.DATE => ((x: Timestamp) => Timestamp.valueOf(x.toLocalDateTime.truncatedTo(ChronoUnit.DAYS)))
    case JSONFieldTypes.DATETIME => ((x:Timestamp) => x)
    case _ => ((x:Timestamp) => x)
  }

  def langs = Try(conf("langs")).getOrElse("en").split(",")

  def notificationTimeOut = Try(conf("notification.timeout").toInt).getOrElse(6)

  def mapBoxAccessToken = Try(conf("mapbox.accesstoken")).getOrElse("no token provided")

//  def filterEqualityPrecisionDouble:Option[Int] = Try(conf("filterEqualityPrecision.double").toInt).toOption
//
//  def prepareDouble = filterEqualityPrecisionDouble match{
//    case None => ((x:Double) => x)
//    case Some(p) => ((x:Double) => roundAt(p)(x))
////    case Some(p) if p<0 => ((x:Double) => roundAt(-p)(x))
//  }
//
//
//
//  def roundAt(p: Int)(n: Double): Double = { val s = math.pow (10, p); (math.round(n) * s) / s }
//  def truncateAt(p: Int)(n: Double): Double = { val s = math.pow (10, p); (math.floor(n) * s) / s }


}
