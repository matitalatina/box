package ch.wsl.box.rest.logic

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.metadata.JSONMetadataFactory
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
class DbActions[T <: ch.wsl.box.model.Entities.profile.api.Table[M],M <: Product](entity:TableQuery[T])(implicit ec:ExecutionContext) extends UglyDBFilters with Logging {
  import ch.wsl.box.model.Entities.profile.api._
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

  def count()(implicit db:Database):Future[Int] =  db.run {
          entity.length.result
  }

  def count(query:JSONQuery)(implicit db:Database):Future[Int] = {
    val q = entity.where(query.filter)

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

  def find(query:JSONQuery)(implicit db:Database, mat: Materializer): Future[Seq[M]] = {

    val q = entity.where(query.filter).sort(query.sort)
    val qPaged = q.page(query.paging).result

    for{
      data <- db.run(qPaged)
    } yield {
      data
    }
  }


  def keys() = for{
    k <- JSONMetadataFactory.keysOf(entity.baseTableRow.tableName)
  } yield{
    k
  }


//  def write(table:String,schema:String,user:String)(implicit ec:ExecutionContext) = Auth.adminDB.run {
//    sql"""SELECT 1
//          FROM information_schema.role_table_grants
//          WHERE table_name=$table and table_schema=$schema and grantee=$user and privilege_type='UPDATE'""".as[Int].headOption.map(_.isDefined)
//  }

//  def ids(query:JSONQuery)(implicit db:Database, mat:Materializer):Future[IDs] = {
////    val x: Seq[Node] = entity.baseTableRow.primaryKeys.flatMap(_.columns).toIndexedSeq
////
////    entity.baseTableRow.primaryKeys.flatMap(_.columns).toSeq.qu
//
//    val q = entity.where(query.filter).sort(query.sort)
//    val qPaged = q.page(query.paging)
//
//    for{
//     keys <-  keys()
//     idsel <- qPaged.map(x => x.reps(keys)).result
//     n <- count()
//
//    } yield {
//
//      val last = query.paging match {
//        case None => true
//        case Some(paging) =>  paging.currentPage * paging.pageLength >= n
//      }
//      import ch.wsl.box.shared.utils.JsonUtils._
//      IDs(
//        last,
//        query.paging.map(_.currentPage).getOrElse(1),
//        idsel.map(x => keys.zip(x).map{case (k,v) => k+"::"+v.toString}.mkString(",")),
////        (Seq.tabulate(idsel.length)(_ => keys)).flatten.zip(idsel).toSeq.map((k,v)=> k+"::"+v.toString).mkString(","),
//        n
//      )
//    }

//
//    val kstr = k.mkString(",")
//    db.run(
//      sql"""SELECT #$
//                FROM information_schema.role_table_grants
//                WHERE table_name=$table and table_schema=$schema and grantee=$user and privilege_type='UPDATE'""".as[Int].headOption.map(_.isDefined))
//
//
//    for{
//      data <- find(query)
//      keys <- keys()
//      n <- count()
//    } yield {
//
//      val last = query.paging match {
//        case None => true
//        case Some(paging) =>  paging.currentPage * paging.pageLength >= n
//      }
//      import ch.wsl.box.shared.utils.JsonUtils._
//      IDs(
//        last,
//        query.paging.map(_.currentPage).getOrElse(1),
//        data.map{_.asJson.ID(keys).asString},
//        n
//      )
//    }
//  }

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
          logger.info(action.statements.toString)
          action
        }
      } yield result.headOption
      case None => Future.successful(None)
    }
  }

  def insert(e: M)(implicit db:Database): Future[T#TableElementType] = {
    logger.info(s"INSERT $e")
    for{
      result <- db.run {
        (entity.returning(entity) += e).transactionally
      }
    } yield result
  }

  def deleteById(id:JSONID)(implicit db:Database):Future[Int] = {
    logger.info(s"DELETE BY ID $id")
    for{
      result <- db.run{
        val action = filter(id).delete
        //println(action.statements)
        action.transactionally
      }
    } yield result
  }

  def updateById(id:JSONID, e:M)(implicit db:Database):Future[Int] = {
    logger.info(s"UPDATE BY ID $id")
    for{
      result <- db.run {
        val action = filter(id).update(e)
        logger.info (action.statements.toString)
        action.transactionally
      }
    } yield result
  }

  def updateIfNeededById(id:JSONID, e:M)(implicit db:Database):Future[Int] = {
    logger.info(s"UPDATE IF NEEDED BY ID $id")
    for {
      current <- getById(id)
    } yield {
      if (current.get != e) {

        val result = db.run {
          val action = filter(id).update(e)
          logger.info(action.statements.toString)
          action.transactionally
        }
        logger.info(s"UPDATED IF NEEDED BY ID $id")
        result
      } else {
        Future.successful(0)
      }
    }
  }.flatMap(identity)

  def upsert(id:JSONID, e:M)(implicit db:Database):Future[Int] = {
    logger.info(s"UPSERT BY ID $id")
    for{
      result <- db.run {
        val action = filter(id).insertOrUpdate(e) //todo: this does not work
        logger.info (action.statements.toString)
        action.transactionally
      }
    } yield result
  }

  def upsertIfNeededById(id:JSONID, e:M)(implicit db:Database):Future[Int] = {
    logger.info(s"UPSERT IF NEEDED BY ID $id")
    for {
      current <- getById(id)
    } yield {
      if (current.isDefined) {
        if (current.get != e) {

          val result = db.run {
            val action = filter(id).update(e)
            logger.info(action.statements.toString)
            action.transactionally
          }
          logger.info(s"UPSERTED (UPDATED) IF NEEDED BY ID $id")
          result
        } else {
          Future.successful(0)
        }
      }else{
        insert(e)
        logger.info(s"UPSERTED (INSERTED) IF NEEDED BY ID $id")
        Future.successful(1)
      }
    }
  }.flatMap(identity)
}