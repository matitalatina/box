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
  def getById(query: JSONKeys)(implicit db:Database): Future[Option[Json]]
  def update(keys:JSONKeys,json: Json)(implicit db:Database):Future[Int]
  def insert(json: Json)(implicit db:Database):Future[Json]
  def count()(implicit db:Database):Future[JSONCount]
  def keyList(query:JSONQuery,table:String)(implicit db:Database):Future[KeyList]
}

case class JsonActions[T <: slick.driver.PostgresDriver.api.Table[M],M <: Product](table:TableQuery[T])(implicit encoder: Encoder[M], decoder: Decoder[M]) extends ModelJsonActions {

  val utils = new DbActions[T,M](table)

  override def getModel(query: JSONQuery)(implicit db:Database): Future[Seq[Json]] = utils.find(query).map(_.data.toSeq.map(_.asJson))
  override def getById(query: JSONKeys)(implicit db:Database): Future[Option[Json]] = utils.getById(query).map(_.map(_.asJson))


  override def count()(implicit db:Database) = {
    db.run {
      table.length.result
    }.map { result =>
      JSONCount(result)
    }
  }

  override def update(keys:JSONKeys,json: Json)(implicit db: _root_.slick.driver.PostgresDriver.api.Database): Future[Int] = {
    for{
      current <- getById(keys)                    //retrieve values in db
      merged = current.get.deepMerge(json)        //merge old and new json
      result <- utils.updateById(keys,merged.as[M].right.get)
    } yield result
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

  override def keyList(query:JSONQuery,table:String)(implicit db:Database):Future[KeyList] = {
    for{
      data <- getModel(query)
      keys <- JSONSchemas.keysOf(table)
      count <- count()
    } yield {
      //println(data.toString().take(100))
      //println(keys)
      val last = query.paging match {
        case None => true
        case Some(paging) =>  paging.page*paging.count >= count.count
      }
      import ch.wsl.box.shared.utils.JsonUtils._
      KeyList(
        last,
        query.paging.map(_.page).getOrElse(1),
        data.map{_.keys(keys).asString},
        count.count
      )
    }
  }
}