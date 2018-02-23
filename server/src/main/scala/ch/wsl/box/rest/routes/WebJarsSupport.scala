package ch.wsl.box.rest.routes

import akka.http.scaladsl.server.Directives._
import org.webjars.WebJarAssetLocator

import scala.util.{Failure, Success, Try}

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
object WebJarsSupport {
  private val webJarAssetLocator = new WebJarAssetLocator

  def webJars = {
    extractUnmatchedPath { path =>
      println("Looking for webjar: " + path.toString().substring(1))
      Try(webJarAssetLocator.getFullPath(path.toString.substring(1))) match {
        case Success(fullPath) => {
          println("found")
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