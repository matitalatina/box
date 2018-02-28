package ch.wsl.box.rest.logic

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import ch.wsl.box.model.shared._
import scribe.Logging
import slick.basic.DatabasePublisher
import slick.jdbc.{ResultSetConcurrency, ResultSetType}
import slick.lifted.{ColumnOrdered, TableQuery}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Created by andreaminetti on 15/03/16.
  */
class DbActions[T <: ch.wsl.box.model.Tables.profile.api.Table[M],M <: Product](entity:TableQuery[T])(implicit ec:ExecutionContext) extends UglyDBFilters with Logging {
  import ch.wsl.box.model.Tables.profile.api._
  import ch.wsl.box.rest.logic.EnhancedTable._ //import col select

  implicit class QueryBuilder(base:Query[T,M,Seq]) {
    def where(filters: Seq[JSONQueryFilter]): Query[T, M, Seq] = {
      filters.foldRight[Query[T, M, Seq]](base) { case (jsFilter, query) =>
        //println(jsFilter)
        query.filter(x => operator(jsFilter.operator.getOrElse(Filter.EQUALS))(x.col(jsFilter.column), jsFilter.value))
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
  }

  def count(query:JSONQuery)(implicit db:Database):Future[Int] = {
    val q = entity.where(query.filter).sort(query.sort)

    for {
      c <- db.run{
        q.length.result
      }
    } yield c
  }

  def findStreamed(query:JSONQuery)(implicit db:Database): DatabasePublisher[M] = {
    val q = entity.where(query.filter).sort(query.sort)
    val qPaged = q.page(query.paging).result
      .withStatementParameters(rsType = ResultSetType.ForwardOnly, rsConcurrency = ResultSetConcurrency.ReadOnly, fetchSize = 0) //needed for PostgreSQL streaming result as stated in http://slick.lightbend.com/doc/3.2.1/dbio.html
      .transactionally

    db.stream(qPaged)

  }

  def find(query:JSONQuery)(implicit db:Database, mat: Materializer): Future[JSONData[M]] = {

    val q = entity.where(query.filter).sort(query.sort)
    val qPaged = q.page(query.paging).result

    for{
      data <- db.run(qPaged)
      count <- count(query)
    } yield {
      JSONData(data.toList,count)
    }
  }

  private def filter(id:JSONID):Query[T, M, Seq]  = {
    if(id.id.isEmpty) throw new Exception("No key is defined")

    def fil(t: Query[T,M,Seq],keyValue: JSONKeyValue):Query[T,M,Seq] =  t.filter(x => super.==(x.col(keyValue.key),keyValue.value))

    id.id.foldRight[Query[T,M,Seq]](entity){case (jsFilter,query) => fil(query,jsFilter)}
  }


  def getById(id:JSONID)(implicit db:Database):Future[Option[T#TableElementType]] = {
    logger.info(s"GET BY ID $id")
    Try(filter(id)).toOption match {
      case Some(f) => for {
        result <- db.run {
          val action = f.take(1).result
          logger.debug(action.statements)
          action
        }
      } yield result.headOption
      case None => Future.successful(None)
    }
  }

  def deleteById(id:JSONID)(implicit db:Database):Future[Int] = {

    for{
      result <- db.run{
        val action = filter(id).delete
        //println(action.statements)
        action
      }
    } yield result
  }

  def updateById(id:JSONID, e:M)(implicit db:Database):Future[Int] = {
    //println (e)
    for{
      result <- db.run {
        val action = filter(id).update(e)
        logger.debug (action.statements)
        action
      }
    } yield result
  }

}