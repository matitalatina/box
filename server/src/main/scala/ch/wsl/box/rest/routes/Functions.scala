package ch.wsl.box.rest.routes
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.rest.jdbc.{JdbcConnect, PostgresProfile}
import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.metadata.{DataMetadataFactory, EntityMetadataFactory, FunctionMetadataFactory}
import ch.wsl.box.rest.utils.UserProfile
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

object Functions extends Data {

  import ch.wsl.box.rest.jdbc.PostgresProfile.api._
  import ch.wsl.box.shared.utils.JSONUtils._

  def context(json:Json) = Context(
    json,
    new RuntimeWS {
      override def get(url: String): Future[String] = ???

      override def post(url: String, data: String): Future[String] = ???
    },
    new RuntimePSQL {
      override def function(name: String, parameters: Seq[Json],lang:String)(implicit ec: ExecutionContext, db: Database): Future[Option[DataResult]] = JdbcConnect.function(name,parameters,lang)

      override def table(name: String,lang:String)(implicit ec: ExecutionContext, up: UserProfile,mat:Materializer): Future[Option[DataResult]] = {

        implicit def db = up.db

        val actions = EntityActionsRegistry().tableActions(name)

        for {
          rows <- actions.find()
        } yield {
          rows.headOption.map { firstRow =>
            DataResult(
              headers = firstRow.asObject.get.keys.toSeq,
              rows = rows.map{ row =>
                row.asObject.get.values.toSeq.map(_.string)
              }
            )
          }
        }

      }
    }
  )


  override def metadataFactory(implicit up: UserProfile, mat: Materializer, ec: ExecutionContext): DataMetadataFactory = FunctionMetadataFactory()

  def functions = ch.wsl.box.model.boxentities.Function

  override def data(function: String, params: Json, lang: String)(implicit up: UserProfile,  mat: Materializer, ec: ExecutionContext): Future[Option[DataResult]] = {
    implicit def db:Database = up.db

    for{
      functionDef <- up.boxDb.run{
        functions.Function.filter(_.name === function).result
      }.map(_.headOption)
      result <- functionDef match {
        case None => Future.successful(None)
        case Some(func) => {
          val f = RuntimeFunction(func.name,func.function)
          f(context(params)).map(Some(_))
        }
      }
    } yield result
  }.recover{case t:Throwable =>
    t.printStackTrace()
    None
  }
}
