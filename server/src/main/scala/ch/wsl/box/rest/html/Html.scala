package ch.wsl.box.rest.html


import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

trait Html{
  def render(html:String,json:Json)(implicit ex:ExecutionContext):Future[String]
}

object Html extends Html {

  private val renderer = new mustache.Mustache()

  def render(html:String,json:Json)(implicit ex:ExecutionContext) = renderer.render(html,json)
}
