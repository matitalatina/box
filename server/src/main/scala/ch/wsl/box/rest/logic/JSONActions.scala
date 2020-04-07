package ch.wsl.box.rest.logic

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.model.shared._
import io.circe._
import io.circe.syntax._
import scribe.Logging
import slick.basic.DatabasePublisher
import ch.wsl.box.jdbc.PostgresProfile
import slick.lifted.TableQuery
import ch.wsl.box.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by andre on 5/19/2017.
  */

trait EntityJSONViewActions {

  def find(query: JSONQuery=JSONQuery.empty)(implicit db: Database, mat:Materializer): Future[Seq[Json]] = Source.fromPublisher(findStreamed(query)).runFold(Seq[Json]())(_ ++ Seq(_))

  def findStreamed(query: JSONQuery=JSONQuery.empty)(implicit db: Database, mat:Materializer): DatabasePublisher[Json]

  def getById(id: JSONID=JSONID.empty)(implicit db: Database): Future[Option[Json]]

  def count()(implicit db: Database): Future[JSONCount]

  def ids(query: JSONQuery)(implicit db: Database, mat:Materializer): Future[IDs]
}


trait EntityJSONTableActions extends EntityJSONViewActions {

  def insert(json: Json)(implicit db:Database):Future[Json]

  def delete(id:JSONID)(implicit db:Database):Future[Int]

  def update(id:JSONID, json: Json)(implicit db:Database):Future[Json]

  def updateIfNeeded(id:JSONID, json: Json)(implicit db:Database):Future[Json]

  def upsert(id:JSONID, json: Json)(implicit db:Database):Future[Json]

  def upsertIfNeeded(id:JSONID, json: Json)(implicit db:Database):Future[Json]
}


case class JSONViewActions[T <: ch.wsl.box.jdbc.PostgresProfile.api.Table[M],M <: Product](entity:TableQuery[T])(implicit encoder: Encoder[M], decoder: Decoder[M], ec:ExecutionContext) extends EntityJSONViewActions {

  val dbActions = new DbActions[T,M](entity)

  override def findStreamed(query: JSONQuery=JSONQuery.empty)(implicit db:Database, mat:Materializer): DatabasePublisher[Json] = dbActions.findStreamed(query).mapResult(_.asJson)

  override def getById(id: JSONID=JSONID.empty)(implicit db:Database): Future[Option[Json]] = dbActions.getById(id).map(_.map(_.asJson))

  override def count()(implicit db:Database) = dbActions.count().map(JSONCount(_))

  override def ids(query:JSONQuery)(implicit db:Database, mat:Materializer):Future[IDs] = {
    for{
      data <- dbActions.find(query)
      keys <- dbActions.keys()   // JSONMetadataFactory.keysOf(table.baseTableRow.tableName)
      n <- dbActions.count(query)
    } yield {

      val last = query.paging match {
        case None => true
        case Some(paging) =>  (paging.currentPage * paging.pageLength) >= n
      }
      import ch.wsl.box.shared.utils.JSONUtils._
      IDs(
        last,
        query.paging.map(_.currentPage).getOrElse(1),
        data.map{_.asJson.ID(keys).asString},
        n
      )
    }
  }

}

case class JSONTableActions[T <: ch.wsl.box.jdbc.PostgresProfile.api.Table[M],M <: Product](table:TableQuery[T])(implicit encoder: Encoder[M], decoder: Decoder[M], ec:ExecutionContext) extends EntityJSONTableActions with Logging {

  lazy val jsonView = JSONViewActions[T,M](table)

  override def findStreamed(query: JSONQuery)(implicit db:Database, mat:Materializer):DatabasePublisher[Json] = jsonView.findStreamed(query)

  override def getById(id: JSONID)(implicit db:Database): Future[Option[Json]] = jsonView.getById(id)

  override def count()(implicit db:Database) = jsonView.count()

  override def ids(query:JSONQuery)(implicit db:Database, mat:Materializer):Future[IDs] = jsonView.ids(query)


  override def update(id:JSONID, json: Json)(implicit db: _root_.ch.wsl.box.jdbc.PostgresProfile.api.Database): Future[Json] = {
    for{
      current <- getById(id) //retrieve values in db
      merged  = current.get.deepMerge(json) //merge old and new json
      _ <- jsonView.dbActions.updateById(id, toM(merged))
    } yield {
      merged //json
    }
  }

  override def updateIfNeeded(id:JSONID, json: Json)(implicit db: _root_.ch.wsl.box.jdbc.PostgresProfile.api.Database): Future[Json] = {
    for{
      current <- getById(id) //retrieve values in db
      merged  = current.get.deepMerge(json) //merge old and new json
      _ <- if (toM(current.get) != toM(merged)) {  //check if same
        jsonView.dbActions.updateById(id, toM(merged))            //could also use updateIfNeeded and no check
      } else Future.successful()
    } yield {
      merged //json
    }
  }

  override def insert(json: Json)(implicit db:Database): Future[Json] = {
    val result: Future[M] = jsonView.dbActions.insert(toM(json))
    result.map(_.asJson)
  }

  override def upsert(id:JSONID, json: Json)(implicit db: _root_.ch.wsl.box.jdbc.PostgresProfile.api.Database): Future[Json] = {
    for{
      current <- getById(id).recover{case _ => None} //retrieve values in db
      result <- if (current.isDefined){   //if exists, check if we have to update
        val merged  = current.get.deepMerge(json) //merge old and new json
        jsonView.dbActions.updateById(id, toM(merged)).map(_ => merged)
      } else{
      //        jsonView.dbActions.upsertById(id, toM(json))
        insert(json).map(_ => json)
      }
    } yield {
      result
    }
  }

  override def upsertIfNeeded(id:JSONID, json: Json)(implicit db: _root_.ch.wsl.box.jdbc.PostgresProfile.api.Database): Future[Json] = {
    for{
      current <- getById(id).recover{case _ => None} //retrieve values in db
      result <- if (current.isDefined){   //if exists, check if we have to skip the update (if row is the same)
        val merged  = current.get.deepMerge(json) //merge old and new json
        if (toM(current.get) != toM(merged)) {
          jsonView.dbActions.updateById(id, toM(merged)).map(_ => merged)          //could also use updateIfNeeded and no check
        } else Future.successful(merged)
      } else{
      //        jsonView.dbActions.upsertById(id, toM(json))
        insert(json).map(_ => json)
      }
    } yield {
      result
    }
  }

  override def delete(id: JSONID)(implicit db: PostgresProfile.api.Database) = jsonView.dbActions.deleteById(id)

  protected def toM(json: Json):M =json.as[M].fold(
      { fail =>
        throw new JSONDecoderException(fail,json)
      },
      { x => x }
  )
}

case class JSONDecoderException(failure: DecodingFailure, original:Json) extends Throwable
