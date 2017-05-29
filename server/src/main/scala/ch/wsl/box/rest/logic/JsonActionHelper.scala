package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.{JSONCount, JSONKeys, JSONQuery, KeyList}
import io.circe._
import io.circe.syntax._
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andre on 5/19/2017.
  */

trait ModelJsonActions {
  def getModel(query:JSONQuery)(implicit db:Database):Future[Seq[Json]]
  def getById(query: JSONKeys)(implicit db:Database): Future[Json]
  def update(keys:JSONKeys,json: Json)(implicit db:Database):Future[Int]
  def insert(json: Json)(implicit db:Database):Future[Json]
  def count()(implicit db:Database):Future[JSONCount]
  def keyList(query:JSONQuery,table:String)(implicit db:Database):Future[KeyList]
}

case class JsonActionHelper[T <: slick.driver.PostgresDriver.api.Table[M],M <: Product](table:TableQuery[T])(implicit encoder: Encoder[M], decoder: Decoder[M]) extends ModelJsonActions {

  val utils = new RouteHelper[T,M](table)

  override def getModel(query: JSONQuery)(implicit db:Database): Future[Seq[Json]] = utils.find(query).map(_.data.toSeq.map(_.asJson))
  override def getById(query: JSONKeys)(implicit db:Database): Future[Json] = utils.getById(query).map(_.asJson)


  override def count()(implicit db:Database) = {
    db.run {
      table.length.result
    }.map { result =>
      JSONCount(result)
    }
  }

  override def update(keys:JSONKeys,json: Json)(implicit db: _root_.slick.driver.PostgresDriver.api.Database): Future[Int] = {
    for{
      current <- getById(keys)
      merged = current.deepMerge(json)
      result <- utils.updateById(keys,merged.as[M].right.get)
    } yield result
  }

  override def insert(json: Json)(implicit db:Database): Future[Json] = {
    println(json)
    val data:M = json.as[M].fold({ fail =>
      println(fail.toString())
      println(fail.history)
      throw new Exception(fail.toString())
    },
    { x => x})
    val result: Future[M] = db.run { table.returning(table) += data }
    result.map(_.asJson)
  }

  override def keyList(query:JSONQuery,table:String)(implicit db:Database):Future[KeyList] = {
    for{
      data <- getModel(query)
      keys <- JSONSchemas.keysOf(table)
      count <- count()
    } yield {
      //println(data.toString().take(100))
      //println(keys)
      val last = query.page*query.count >= count.count
      import ch.wsl.box.shared.utils.JsonUtils._
      KeyList(
        last,
        query.page,
        data.map{_.keys(keys).asString},
        count.count
      )
    }
  }
}