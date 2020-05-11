package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST
import scala.util.Try

object UI {

  import ch.wsl.box.client.Context._
  import io.circe._
  import io.circe.generic.auto._
  import io.circe.syntax._


  private var ui:Map[String,String] = Map()

  def load() = REST.ui().map { ui =>
    this.ui = ui
    println(ui)
  }

  case class MenuEntry(name:String,url:String)

  def logo = ui.lift("logo")
  def title = ui.lift("title")
  def indexTitle = Labels(ui.lift("index.title").getOrElse("ui.index.title"))
  def indexHtml = Labels(ui.lift("index.html").getOrElse(""))
  def newsTable = ui.lift("newsTable")
  def footerCopyright = ui.lift("footerCopyright")
  def debug = ui.lift("debug").contains("true")
  def enableAllTables = ui.lift("enableAllTables").contains("true")
  def showEntitiesSidebar = ui.lift("showEntitiesSidebar").contains("true")
  def menu = ui.lift("menu").toSeq.flatMap{ m =>
    Try {
      parser.parse(m).right.get.as[Seq[MenuEntry]].right.get
    }.toOption
  }.flatten


}
