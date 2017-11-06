package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.Try

/**
  * Created by andre on 6/8/2017.
  */
object Conf {

  private var conf:Map[String,String] = Map()

  def load() = REST.conf().map { table =>
    conf = table
  }

  def pageLength = Try(conf("page_length").toInt).getOrElse(30)
}
