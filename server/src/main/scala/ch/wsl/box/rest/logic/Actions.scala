package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.shared.{IDs, JSONCount, JSONID, JSONQuery}
import ch.wsl.box.jdbc.PostgresProfile.api._
import slick.basic.DatabasePublisher

import scala.concurrent.Future
import akka.stream.scaladsl.Source
import ch.wsl.box.jdbc.FullDatabase



trait ViewActions[T] {

  def find(query: JSONQuery=JSONQuery.empty)(implicit db: FullDatabase, mat:Materializer): DBIO[Seq[T]]

  def findStreamed(query: JSONQuery=JSONQuery.empty)(implicit db: FullDatabase): DatabasePublisher[T]

  def getById(id: JSONID=JSONID.empty)(implicit db: FullDatabase):DBIO[Option[T]]

  def count()(implicit db: FullDatabase): DBIO[JSONCount]
  def count(query: JSONQuery)(implicit db: FullDatabase): DBIO[Int]

  def ids(query: JSONQuery)(implicit db: FullDatabase, mat:Materializer): DBIO[IDs]
}

/**
 *
 * Modification actions always return the number of modificated rows
 * we decided to not return the model itself because in some cirumstances
 * it may exposes more information than required, in this way we assure
 * that data retrival system is uniform
 *
 * @tparam T model class type
 */
trait TableActions[T] extends ViewActions[T] {
  def insert(obj: T)(implicit db:FullDatabase): DBIO[JSONID]

  def delete(id:JSONID)(implicit db:FullDatabase): DBIO[Int]

  def update(id:JSONID, obj: T)(implicit db:FullDatabase): DBIO[Int]

  def updateIfNeeded(id:JSONID, obj: T)(implicit db:FullDatabase): DBIO[Int]

  def upsertIfNeeded(id:Option[JSONID], obj: T)(implicit db:FullDatabase): DBIO[JSONID]
}
