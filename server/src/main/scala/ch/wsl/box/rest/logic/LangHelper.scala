package ch.wsl.box.rest.logic

import ch.wsl.box.jdbc.Connection
import ch.wsl.box.model.boxentities.BoxLabels
import ch.wsl.box.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by andre on 6/8/2017.
  */
case class LangHelper(lang:String)(implicit ec:ExecutionContext) {
  def translationTable:Future[Map[String,String]] = {
    val query = for{
      label <- BoxLabels.BoxLabelsTable if label.lang === lang
    } yield label
    Connection.adminDB.run(query.result).map{_.map{ row =>
      row.key -> row.label.getOrElse("")
    }.toMap}
  }
}
