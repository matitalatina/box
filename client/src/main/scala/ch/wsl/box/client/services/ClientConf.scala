package ch.wsl.box.client.services

import java.sql.Timestamp
import java.time.temporal.ChronoUnit

import ch.wsl.box.client.styles.constants.StyleConstants
import ch.wsl.box.client.styles.constants.StyleConstants.{ChildProperties, Colors}
import ch.wsl.box.client.styles.{GlobalStyles, StyleConf}
import ch.wsl.box.model.shared.JSONFieldTypes
import io.circe._
import io.circe.parser._

import scala.util.Try

/**
  * Created by andre on 6/8/2017.
  */


object ClientConf {

  private var conf:Map[String,String] = Map()
  private var _version:String = ""
  private var _appVersion:String = ""

  def load(table:Map[String,String],version:String,appVersion:String) = {
    conf = table
    _version = version
    _appVersion = appVersion
  }

  def version = _version
  def appVersion = _appVersion

  def pageLength  = Try(conf("page_length").toInt).getOrElse(30)
//  def lookupMaxRows  = Try(conf("fk_rows").toInt).getOrElse(30)

  def manualEditKeyFields = Try(conf("pks.edit").toBoolean).getOrElse(false)
  def manualEditSingleKeyFields = Try(conf("pks.edit.single").trim().replace(' ',',').split(",").toSeq).getOrElse(Seq[String]())

  def displayIndexNews = Try(conf("display.index.news").toBoolean).getOrElse(false)
  def displayIndexHtml = Try(conf("display.index.html").toBoolean).getOrElse(false)

  def menuSeparator = Try(conf("menu.separator")).getOrElse(" ")

  def colorMain = Try(conf("color.main")).getOrElse("#006268")
  def colorMainText = Try(conf("color.main.text")).getOrElse("#ffffff")
  def colorMainLink = Try(conf("color.main.link")).getOrElse(colorMain)
  def colorLink = Try(conf("color.link")).getOrElse("#fbf0b2")
  def colorDanger = Try(conf("color.danger")).getOrElse("#4c1c24")
  def colorWarning = Try(conf("color.warning")).getOrElse("#ffa500")

  def tableFontSize = Try(conf("table.fontSize").toInt).getOrElse(10)

  def childBorderSize = Try(conf("child.border.size").toInt).getOrElse(1)
  def childBorderColor = Try(conf("child.border.color")).getOrElse(StyleConstants.Colors.GreySemi.value)
  def childPaddingSize = Try(conf("child.padding.size").toInt).getOrElse(10)
  def childMarginTopSize = Try(conf("child.marginTop.size").toInt).getOrElse(-1)
  def childBackgroundColor = Try(conf("child.backgroundColor")).getOrElse(StyleConstants.Colors.GreyExtra.value)

  lazy val style = GlobalStyles(StyleConf(colors = Colors(colorMain,colorMainText,colorMainLink,colorLink,colorDanger,colorWarning),
                                          tableFontSize,
                                          ChildProperties(childBorderSize, childBorderColor, childPaddingSize,
                                            childMarginTopSize, childBackgroundColor)  ))

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

  def mapOptions = Try(parse(conf("map.options")).right.get).getOrElse(Json.Null)

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
