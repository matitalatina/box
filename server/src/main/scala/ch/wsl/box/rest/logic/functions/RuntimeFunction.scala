package ch.wsl.box.rest.logic.functions

import akka.actor.ActorSystem
import akka.stream.Materializer
import ch.wsl.box.model.shared.JSONQuery
import ch.wsl.box.rest.utils.{BoxConfig, Lang, UserProfile}
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.logic.{DataResult, DataResultTable}
import delight.graaljssandbox.{GraalSandbox, GraalSandboxes}


trait RuntimeWS{
  def get(url: String)(implicit ec:ExecutionContext, mat:Materializer, system:ActorSystem):Future[String]
  def post(url:String,data:String, contentType:String = "text/plain; charset=UTF-8")(implicit ec:ExecutionContext, mat:Materializer, system:ActorSystem):Future[String]
}

trait RuntimePSQL{
  def function(name:String,parameters:Seq[Json])(implicit lang:Lang, ec:ExecutionContext,db:Database):Future[Option[DataResultTable]]
  def table(name:String, query:JSONQuery = JSONQuery.empty)(implicit lang:Lang, ec:ExecutionContext, up:UserProfile, mat:Materializer):Future[Option[DataResultTable]]
}

case class Context(data:Json,ws:RuntimeWS,psql:RuntimePSQL)

object RuntimeFunction {

  def context(json:Json) = Context(
    json,
    WSImpl,
    PSQLImpl
  )

  private val compiledFunctions = scala.collection.mutable.Map[String,GraalSandbox]()

  def resetCache() = {
    compiledFunctions.clear()
  }


  private def compile(embedded:String) = {
    val sandbox = GraalSandboxes.create()
    sandbox.inject("ws",WSImpl)
    sandbox.inject("psql",PSQLImpl)
    sandbox
  }

  def apply(name:String,embedded:String)(implicit ec:ExecutionContext,up:UserProfile,mat:Materializer,system:ActorSystem): (Context,String) => Future[DataResult] = {

    val function = compiledFunctions.get(name) match {
      case Some(f) => f
      case None => {
        val result = compile(embedded)
        if(BoxConfig.enableCache) {
          compiledFunctions += (name -> result)
        }
        result
      }
    }

    function(ec,up,mat,system)

  }
}
