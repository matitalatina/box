package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared._
import slick.lifted.{ColumnOrdered, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * Created by andreaminetti on 15/03/16.
  */
class DbActions[T <: ch.wsl.box.model.tables.profile.api.Table[M],M <: Product](table:TableQuery[T]) extends UglyDBFilters {
  import ch.wsl.box.model.tables.profile.api._
  import ch.wsl.box.rest.logic.EnhancedTable._ //import col select

  def find(query:JSONQuery)(implicit db:Database):Future[JSONResult[M]] = {
    val qFiltered = query.filter.foldRight[Query[T,M,Seq]](table){case (jsFilter,query) =>
      //println(jsFilter)
      query.filter(x => operator(jsFilter.operator.getOrElse(Filter.EQUALS))(x.col(jsFilter.column),jsFilter.value))
    }

    val qSorted = query.sort.foldRight[Query[T,M,Seq]](qFiltered){case (sort,query) =>
      query.sortBy{ x =>
        val c:Rep[_] = x.col(sort.column).rep
        sort.order.toLowerCase() match {
          case Sort.ASC => ColumnOrdered(c,new slick.ast.Ordering)
          case Sort.DESC => ColumnOrdered(c,new slick.ast.Ordering(direction=slick.ast.Ordering.Desc))
        }
      }
    }

    val qPaged:Query[T, M, Seq] = query.paging match {
      case None => qSorted
      case Some(paging) => qSorted.drop ((paging.page - 1) * paging.count).take (paging.count)
    }

    for {
      result <- db.run {
        val r = qPaged.result;
        //r.statements.foreach(println);
        r
      }
      count <- db.run{ qSorted.length.result }
    } yield JSONResult(count,result.toList) // to list because json4s does't like generics types for serialization
  }

  private def filter(i:JSONKeys):Query[T, M, Seq]  = {
    def fil(t: Query[T,M,Seq],key: JSONKey):Query[T,M,Seq] =  t.filter(x => super.==(x.col(key.key),key.value))

    i.keys.foldRight[Query[T,M,Seq]](table){case (jsFilter,query) => fil(query,jsFilter)}
  }


  def getById(i:JSONKeys)(implicit db:Database):Future[Option[T#TableElementType]] = {
    println(s"GET BY ID $i")
    Try(filter(i)).toOption match {
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

  def deleteById(i:JSONKeys)(implicit db:Database):Future[Int] = {

    for{
      result <- db.run{
        val action = filter(i).delete
        //println(action.statements)
        action
      }
    } yield result
  }

  def updateById(i:JSONKeys,e:M)(implicit db:Database):Future[Int] = {
    //println (e)
    for{
      result <- db.run {
        val action = filter(i).update(e)
        //println (action.statements)
        action
      }
    } yield result
  }

}