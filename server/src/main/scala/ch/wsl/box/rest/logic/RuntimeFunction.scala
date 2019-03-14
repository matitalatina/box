package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.rest.utils.{Eval, UserProfile}
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}
import ch.wsl.box.rest.jdbc.PostgresProfile.api._


trait RuntimeWS{
  def get(url:String):Future[String]
  def post(url:String,data:String):Future[String]
}

trait RuntimePSQL{
  def function(name:String,parameters:Seq[Json],lang:String)(implicit ec:ExecutionContext,db:Database):Future[Option[DataResult]]
  def table(name:String,lang:String)(implicit ec:ExecutionContext, up:UserProfile, mat:Materializer):Future[Option[DataResult]]
}

case class Context(data:Json,ws:RuntimeWS,psql:RuntimePSQL)

object RuntimeFunction {

  private val compiledFunctions = scala.collection.mutable.Map[String,(ExecutionContext,UserProfile,Materializer) => (Context => Future[DataResult])]()

  def resetCache() = {
    compiledFunctions.clear()
  }


  private def compile(embedded:String) = {
    val code = s"""
                  |import io.circe._
                  |import io.circe.syntax._
                  |import scala.concurrent.{ExecutionContext, Future}
                  |import ch.wsl.box.shared.utils.JSONUtils._
                  |import ch.wsl.box.rest.logic.Context
                  |import ch.wsl.box.rest.logic.DataResult
                  |import ch.wsl.box.rest.jdbc.PostgresProfile.api._
                  |import akka.stream.Materializer
                  |import ch.wsl.box.rest.utils.UserProfile
                  |
       |(ec:ExecutionContext,up:UserProfile,mat:Materializer) => { (context:Context) => {
                  |implicit def ecImpl = ec
                  |implicit def dbImpl = up.db
                  |implicit def upImpl = up
                  |implicit def matImpl = mat
                  |$embedded
                  |}}
                  |
     """.stripMargin

    Eval[(ExecutionContext,UserProfile,Materializer) => (Context => Future[DataResult])](code)
  }

  def apply(name:String,embedded:String)(implicit ec:ExecutionContext,up:UserProfile,mat:Materializer): Context => Future[DataResult] = {

    val function = compiledFunctions.get(name) match {
      case Some(f) => f
      case None => {
        val result = compile(embedded)
        compiledFunctions += (name -> result)
        result
      }
    }

    function(ec,up,mat)

  }
}
