package ch.wsl.box.client.services

import scala.util.Try

object UI {

  import io.circe._
  import io.circe.generic.auto._


  private var ui:Map[String,String] = Map()

  def load(ui:Map[String,String]) = {
    this.ui = ui
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
