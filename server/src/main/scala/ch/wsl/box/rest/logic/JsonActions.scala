package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.{JSONCount, JSONID, JSONQuery, IDs}
import io.circe._
import io.circe.syntax._
import slick.driver.PostgresDriver
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andre on 5/19/2017.
  */

trait EntityJsonViewActions {
  def getEntity(query: JSONQuery=JSONQuery.empty)(implicit db: Database): Future[Seq[Json]]

  def getById(id: JSONID=JSONID.empty)(implicit db: Database): Future[Option[Json]]

  def count()(implicit db: Database): Future[JSONCount]

  def ids(query: JSONQuery)(implicit db: Database): Future[IDs]
}

trait EntityJsonTableActions extends EntityJsonViewActions {
  def update(id:JSONID, json: Json)(implicit db:Database):Future[Json]

  def delete(id:JSONID)(implicit db:Database):Future[Int]

  def insert(json: Json)(implicit db:Database):Future[Json]
}

case class JsonViewActions[T <: slick.driver.PostgresDriver.api.Table[M],M <: Product](table:TableQuery[T])(implicit encoder: Encoder[M], decoder: Decoder[M]) extends EntityJsonViewActions {

  val utils = new DbActions[T,M](table)

  override def getEntity(query: JSONQuery=JSONQuery.empty)(implicit db:Database): Future[Seq[Json]] = utils.find(query).map(_.data.toSeq.map(_.asJson))

  override def getById(id: JSONID=JSONID.empty)(implicit db:Database): Future[Option[Json]] = utils.getById(id).map(_.map(_.asJson))


  override def count()(implicit db:Database) = {
    db.run {
      table.length.result
    }.map { result =>
      JSONCount(result)
    }
  }

  override def ids(query:JSONQuery)(implicit db:Database):Future[IDs] = {
    for{
      data <- utils.find(query)
      keys <- JSONMetadataFactory.keysOf(table.baseTableRow.tableName)
      countAllRows <- count().map(_.count) //added by bp
    } yield {
      //println(data.toString().take(100))
      //println(keys)
      val last = query.paging match {
        case None => true
        case Some(paging) =>  paging.currentPage * paging.pageLength >= countAllRows //data.count
      }
      import ch.wsl.box.shared.utils.JsonUtils._
      IDs(
        last,
        query.paging.map(_.currentPage).getOrElse(1),
        data.data.map{_.asJson.ID(keys).asString},
        countAllRows //data.count
      )
    }
  }

}

case class JsonTableActions[T <: slick.driver.PostgresDriver.api.Table[M],M <: Product](table:TableQuery[T])(implicit encoder: Encoder[M], decoder: Decoder[M]) extends EntityJsonTableActions {

  lazy val jsonView = JsonViewActions[T,M](table)

  override def getEntity(query: JSONQuery)(implicit db:Database): Future[Seq[Json]] = jsonView.getEntity(query)

  override def getById(id: JSONID)(implicit db:Database): Future[Option[Json]] = jsonView.getById(id)

  override def count()(implicit db:Database) = jsonView.count()

  override def ids(query:JSONQuery)(implicit db:Database):Future[IDs] = jsonView.ids(query)


  override def update(id:JSONID, json: Json)(implicit db: _root_.slick.driver.PostgresDriver.api.Database): Future[Json] = {
    for{
      current <- getById(id) //retrieve values in db
      merged = current.get.deepMerge(json) //merge old and new json
      result <- jsonView.utils.updateById(id,merged.as[M].right.get)
    } yield json
  }

  override def insert(json: Json)(implicit db:Database): Future[Json] = {
    val data:M = json.as[M].fold({ fail =>
      println(fail.toString())
      println(fail.history)
      throw new Exception(fail.toString())
    },
      { x => x})
    println(s"JSON to save on $table: \n $data")
    val result: Future[M] = db.run { table.returning(table) += data }
    result.map(_.asJson)
  }

  override def delete(id: JSONID)(implicit db: PostgresDriver.api.Database) = jsonView.utils.deleteById(id)
}