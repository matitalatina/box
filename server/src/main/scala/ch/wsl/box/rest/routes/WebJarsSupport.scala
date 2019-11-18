package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.Directives._
import org.webjars.WebJarAssetLocator
import scribe.Logging

import scala.util.{Failure, Success, Try}

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
object WebJarsSupport extends Logging {
  private val webJarAssetLocator = new WebJarAssetLocator

  def webJars = {
    extractUnmatchedPath { path =>
      val webjarName = path.toString().substring(1)
      logger.info("Looking for webjar: " + webjarName )
      Try(webJarAssetLocator.getFullPath(webjarName)) match {
        case Success(fullPath) => {
          logger.info("found")
          getFromResource(fullPath)
        }
        case Failure(_: IllegalArgumentException) =>
          reject
        case Failure(e) =>
          failWith(e)
      }
    }
  }
}
