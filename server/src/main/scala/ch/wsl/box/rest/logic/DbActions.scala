package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared._
import slick.lifted.{ColumnOrdered, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * Created by andreaminetti on 15/03/16.
  */
class DbActions[T <: ch.wsl.box.model.Tables.profile.api.Table[M],M <: Product](entity:TableQuery[T]) extends UglyDBFilters {
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

  def find(query:JSONQuery)(implicit db:Database):Future[JSONData[M]] = {
    val q = entity.where(query.filter).sort(query.sort)
    val qPaged = q.page(query.paging)


    for {
      result <- db.run {
        val r = qPaged.result;
        //r.statements.foreach(println);
        r
      }
      count <- db.run{ q.length.result }
    } yield JSONData(result.toList, count) // to list because json4s does't like generics types for serialization
  }

  private def filter(id:JSONID):Query[T, M, Seq]  = {
    if(id.id.isEmpty) throw new Exception("No key is defined")

    def fil(t: Query[T,M,Seq],keyValue: JSONKeyValue):Query[T,M,Seq] =  t.filter(x => super.==(x.col(keyValue.key),keyValue.value))

    id.id.foldRight[Query[T,M,Seq]](entity){case (jsFilter,query) => fil(query,jsFilter)}
  }


  def getById(id:JSONID)(implicit db:Database):Future[Option[T#TableElementType]] = {
    println(s"GET BY ID $id")
    Try(filter(id)).toOption match {
      case Some(f) => for {
        result <- db.run {
          val action = f.take(1).result
          println(action.statements)
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
        println (action.statements)
        action
      }
    } yield result
  }

}