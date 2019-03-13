package ch.wsl.box.rest.logic

import ch.wsl.box.rest.utils.Eval
import io.circe.Json

import scala.concurrent.Future


trait RuntimeWS{
  def get(url:String):Future[String]
  def post(url:String,data:String):Future[String]
}

trait RuntimePSQL{
  def function(name:String,parameters:Seq[Json]):Future[Seq[Seq[String]]]
}

case class Context(data:Json,ws:RuntimeWS,psql:RuntimePSQL)

object RuntimeFunction {


  def apply(embedded:String) = {
    val code = s"""
       |import io.circe._
       |import io.circe.syntax._
       |import scala.concurrent.Future
       |import ch.wsl.box.shared.utils.JSONUtils._
       |import ch.wsl.box.rest.logic.Context
       |
       |(context:Context) => {
       |$embedded
       |}
       |
     """.stripMargin

    Eval[Context => Future[Seq[Seq[String]]]](code)

  }
}
