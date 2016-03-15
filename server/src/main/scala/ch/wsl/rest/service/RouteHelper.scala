package ch.wsl.rest.service

import ch.wsl.jsonmodels.{JSONQuery, JSONResult}
import ch.wsl.rest.domain.{JSONSchemas, UglyDBFilters}
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

  import ch.wsl.rest.domain.EnhancedTable._ //import col select

  def find(query:JSONQuery)(implicit mar:Marshaller[M], unmar: Unmarshaller[M], db:Database):Future[JSONResult[M]] = {
    val qFiltered = query.filter.foldRight[Query[T,M,Seq]](table){case ((field,jsFilter),query) =>
      println(jsFilter)
      query.filter(x => operator(jsFilter.operator.getOrElse("="))(x.col(field),jsFilter.value))
    }

    val qSorted = query.sorting.foldRight[Query[T,M,Seq]](qFiltered){case ((field,dir),query) =>
      query.sortBy{ x =>
        val c:slick.driver.PostgresDriver.api.Rep[_] = x.col(field).rep
        dir match {
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

  def getById(i:Long)(implicit db:Database):Future[T#TableElementType] = {
    def fil(pk:String):Rep[Seq[T#TableElementType]] =  table.filter(x => super.==(x.col(pk),i)).take(1)

    for{
      pks <- JSONSchemas.keysOf(name)
      result <- db.run{
        val action = fil(pks.head).result
        println(action.statements)
        action
      }
    } yield result.head
  }

  def deleteById(i:Long)(implicit db:Database):Future[Int] = {
    println("Deleting " + i)
    def fil(pk:String) =  {
      println("Deleting " + pk + "=" + i)
      table.filter(x => super.==(x.col(pk),i))
    }

    for{
      pks <- JSONSchemas.keysOf(name)
      result <- db.run{
        val action = fil(pks.head).delete
        println(action.statements)
        action
      }
    } yield result
  }

}