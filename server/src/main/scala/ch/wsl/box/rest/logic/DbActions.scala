package ch.wsl.box.rest.logic

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import ch.wsl.box.jdbc.PostgresProfile
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.metadata.{EntityMetadataFactory, FormMetadataFactory}
import scribe.Logging
import slick.ast.Node
import slick.basic.DatabasePublisher
import slick.dbio.Effect
import slick.jdbc.{ResultSetConcurrency, ResultSetType}
import slick.lifted.{ColumnOrdered, TableQuery}
import slick.sql.FixedSqlStreamingAction

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Created by andreaminetti on 15/03/16.
  */
class DbActions[T <: ch.wsl.box.jdbc.PostgresProfile.api.Table[M],M <: Product](entity:TableQuery[T])(implicit ec:ExecutionContext) extends TableActions[M] with DBFiltersImpl with Logging {
  import ch.wsl.box.jdbc.PostgresProfile.api._
  import ch.wsl.box.rest.logic.EnhancedTable._ //import col select

  implicit class QueryBuilder(base:Query[T,M,Seq]) {

    def where(filters: Seq[JSONQueryFilter]): Query[T, M, Seq] = {
      filters.foldRight[Query[T, M, Seq]](base) { case (jsFilter, query) =>
//        println("--------------------------"+jsFilter)
        query.filter(x => operator(jsFilter.operator.getOrElse(Filter.EQUALS))(x.col(jsFilter.column), jsFilter))
      }
    }

    def sort(sorting: Seq[JSONSort]): Query[T, M, Seq] = {
      sorting.foldRight[Query[T, M, Seq]](base) { case (sort, query) =>
        query.sortBy { x =>
          val c: Rep[_] = x.col(sort.column).rep
          sort.order.toLowerCase() match {
            case Sort.ASC => ColumnOrdered(c, new slick.ast.Ordering)
            case Sort.DESC => ColumnOrdered(c, new slick.ast.Ordering(direction = slick.ast.Ordering.Desc))
          }
        }
      }
    }

    def page(paging:Option[JSONQueryPaging]): Query[T, M, Seq] = paging match {
      case None => base
      case Some(paging) => base.drop ((paging.currentPage - 1) * paging.pageLength).take (paging.pageLength)
    }

//    def select(fields:Seq[String]): Query[T, _, Seq] =  base.map(x => x.reps(fields))
  }

  private def resetMetadataCache(): Unit = {
    FormMetadataFactory.resetCacheForEntity(entity.baseTableRow.tableName)
    EntityMetadataFactory.resetCacheForEntity(entity.baseTableRow.tableName)
  }


  def count()(implicit db:Database):DBIO[JSONCount] = {
    entity.length.result
  }.transactionally.map(JSONCount)

  def count(query:JSONQuery)(implicit db:Database):DBIO[Int] = {
    val q = entity.where(query.filter)

    (q.length.result).transactionally

  }


  def findStreamed(query:JSONQuery)(implicit db:Database): DatabasePublisher[M] = {
    val q = entity.where(query.filter).sort(query.sort)
    val qPaged = q.page(query.paging).result
      .withStatementParameters(rsType = ResultSetType.ForwardOnly, rsConcurrency = ResultSetConcurrency.ReadOnly, fetchSize = 0) //needed for PostgreSQL streaming result as stated in http://slick.lightbend.com/doc/3.2.1/dbio.html
      .transactionally


    db.stream(qPaged)

  }

  override def find(query:JSONQuery)(implicit db:Database, mat: Materializer): DBIO[Seq[M]] = {

    val q = entity.where(query.filter).sort(query.sort)
    val qPaged = q.page(query.paging).result

    qPaged.transactionally
  }

  def keys(): DBIOAction[Seq[String], NoStream, Effect] = DBIO.from(EntityMetadataFactory.keysOf(entity.baseTableRow.tableName))

  override def ids(query: JSONQuery)(implicit db: PostgresProfile.api.Database, mat: Materializer): DBIO[IDs] = {
    for{
      data <- find(query)
      keys <- keys()
      n <- count(query)
    } yield {

      val last = query.paging match {
        case None => true
        case Some(paging) =>  (paging.currentPage * paging.pageLength) >= n
      }
      import ch.wsl.box.shared.utils.JSONUtils._
      IDs(
        last,
        query.paging.map(_.currentPage).getOrElse(1),
        data.map{x => new EnhancedModel(x).ID(keys).asString},
        n
      )
    }
  }.transactionally


  private def filter(id:JSONID):Query[T, M, Seq]  = {
    if(id.id.isEmpty) throw new Exception("No key is defined")

    def fil(t: Query[T,M,Seq],keyValue: JSONKeyValue):Query[T,M,Seq] =  t.filter(x => super.==(x.col(keyValue.key),keyValue.value))

    id.id.foldRight[Query[T,M,Seq]](entity){case (jsFilter,query) => fil(query,jsFilter)}
  }


  def getById(id:JSONID)(implicit db:Database) = {
    logger.info(s"GET BY ID $id")
    Try(filter(id)).toOption match {
      case Some(f) => for {
        result <-  {
          val action = f.take(1).result
          logger.info(action.statements.toString)
          action
        }.transactionally
      } yield result.headOption
      case None => DBIO.successful(None)
    }
  }

  def insert(e: M)(implicit db:Database) = {
    logger.info(s"INSERT $e")
    resetMetadataCache()
    for{
      result <-  {
        (entity.returning(entity) += e)
      }.transactionally
      keys <- keys()
    } yield new EnhancedModel(result).ID(keys)
  }

  def delete(id:JSONID)(implicit db:Database) = {
    logger.info(s"DELETE BY ID $id")
    resetMetadataCache()
    filter(id).delete.transactionally
  }

  def update(id:JSONID, e:M)(implicit db:Database) = {
    logger.info(s"UPDATE BY ID $id")
    resetMetadataCache()
    filter(id).update(e).transactionally
  }

  def updateIfNeeded(id:JSONID, e:M)(implicit db:Database) = {
    logger.info(s"UPDATE IF NEEDED BY ID $id")
    resetMetadataCache()
    for {
      current <- getById(id)
      updated <- if (current.get != e) {
        update(id,e).transactionally
      } else {
        DBIO.successful(0)
      }
    } yield updated
  }


  def upsertIfNeeded(id:JSONID, e:M)(implicit db:Database) = {
    logger.info(s"UPSERT IF NEEDED BY ID $id")
    resetMetadataCache()
    for {
      current <- getById(id)
      upserted <- if (current.isDefined) {
        if (current.get != e) {

          val result = update(id,e).transactionally
          logger.info(s"UPSERTED (UPDATED) IF NEEDED BY ID $id")
          result.map(_ => id)
        } else {
          DBIO.successful(id)
        }
      }else{
        val result = insert(e)
        logger.info(s"UPSERTED (INSERTED) IF NEEDED BY ID $id")
        result.transactionally
      }
    } yield upserted
  }


}