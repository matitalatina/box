package ch.wsl.box.rest.logic.functions

import akka.stream.Materializer
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.rest.jdbc.JdbcConnect
import ch.wsl.box.rest.logic.{DataResult, DataResultTable}
import ch.wsl.box.rest.utils.{Lang, UserProfile}
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

object PSQLImpl extends RuntimePSQL {

  import ch.wsl.box.rest.jdbc.PostgresProfile.api._
  import ch.wsl.box.shared.utils.JSONUtils._


  override def function(name: String, parameters: Seq[Json])(implicit lang: Lang,ec: ExecutionContext, db: Database): Future[Option[DataResult]] = JdbcConnect.function(name,parameters,lang.lang)

  override def table(name: String)(implicit lang:Lang, ec: ExecutionContext, up: UserProfile,mat:Materializer): Future[Option[DataResult]] = {

    implicit def db = up.db

    val actions = EntityActionsRegistry().tableActions(name)

    for {
      rows <- actions.find()
    } yield {
      rows.headOption.map { firstRow =>
        DataResultTable(
          headers = firstRow.asObject.get.keys.toSeq,
          rows = rows.map{ row =>
            row.asObject.get.values.toSeq.map(_.string)
          }
        )
      }
    }

  }
}
