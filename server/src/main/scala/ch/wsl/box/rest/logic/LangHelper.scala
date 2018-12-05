package ch.wsl.box.rest.logic

import ch.wsl.box.rest.boxentities.Labels
import ch.wsl.box.rest.utils.Auth
import ch.wsl.box.rest.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by andre on 6/8/2017.
  */
case class LangHelper(lang:String)(implicit ec:ExecutionContext) {
  def translationTable:Future[Map[String,String]] = {
    val query = for{
      label <- Labels.table if label.lang === lang
    } yield label
    Auth.boxDB.run(query.result).map{_.map{ row =>
      row.key -> row.label.getOrElse("")
    }.toMap}
  }
}
