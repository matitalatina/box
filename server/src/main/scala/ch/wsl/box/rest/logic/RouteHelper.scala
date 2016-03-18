package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.{JSONKey, JSONKeys, JSONQuery, JSONResult}
import slick.driver.PostgresDriver.api._
import slick.lifted.{ColumnOrdered, Query, Rep, TableQuery}
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by andreaminetti on 15/03/16.
  */
class RouteHelper[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T]) extends UglyDBFilters {

  import ch.wsl.box.rest.logic.EnhancedTable._ //import col select

  def find(query:JSONQuery)(implicit mar:Marshaller[M], unmar: Unmarshaller[M], db:Database):Future[JSONResult[M]] = {
    val qFiltered = query.filter.foldRight[Query[T,M,Seq]](table){case (jsFilter,query) =>
      println(jsFilter)
      query.filter(x => operator(jsFilter.operator.getOrElse("="))(x.col(jsFilter.column),jsFilter.value))
    }

    val qSorted = query.sort.foldRight[Query[T,M,Seq]](qFiltered){case (sort,query) =>
      query.sortBy{ x =>
        val c:slick.driver.PostgresDriver.api.Rep[_] = x.col(sort.column).rep
        sort.order.toLowerCase() match {
          case "asc" => ColumnOrdered(c,new slick.ast.Ordering)
          case "desc" => ColumnOrdered(c,new slick.ast.Ordering(direction=slick.ast.Ordering.Desc))
        }
      }
    }

    val qPaged:Rep[Seq[T#TableElementType]] = qSorted.drop((query.page - 1) * query.count).take(query.count)

    for {
      result <- db.run { qPaged.result }
      count <- db.run{ qSorted.length.result }
    } yield JSONResult(count,result.toList) // to list because json4s does't like generics types for serialization
  }

  private def filter(i:JSONKeys):Query[T, M, Seq]  = {
    def fil(t: Query[T,M,Seq],key: JSONKey):Query[T,M,Seq] =  t.filter(x => super.==(x.col(key.key),key.value))

    i.keys.foldRight[Query[T,M,Seq]](table){case (jsFilter,query) => fil(query,jsFilter)}
  }


  def getById(i:JSONKeys)(implicit db:Database):Future[T#TableElementType] = {

    for{
      result <- db.run{
        val action = filter(i).take(1).result
        println(action.statements)
        action
      }
    } yield result.head
  }

  def deleteById(i:JSONKeys)(implicit db:Database):Future[Int] = {

    for{
      result <- db.run{
        val action = filter(i).delete
        println(action.statements)
        action
      }
    } yield result
  }

}