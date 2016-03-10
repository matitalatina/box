package ch.wsl.rest.service

import ch.wsl.jsonmodels.{JSONCount, JSONResult, JSONQuery}
import ch.wsl.rest.domain._
import org.json4s.JsonAST._
import slick.lifted.ColumnOrdered
import spray.http.StatusCodes
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import spray.routing.PathMatchers.IntNumber
import spray.routing._

import ch.wsl.model.tables._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.PostgresDriver.api._

import ch.wsl.rest.domain.EnhancedTable._

import scala.util.{Failure, Success}

/**
 * Created by andreaminetti on 16/02/16.
 */




class ModelUtils[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T]) extends UglyDBFilters {
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


trait ModelRoutes extends HttpService {

  var models = Set[String]()

  def model[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T])(implicit mar:Marshaller[M], unmar: Unmarshaller[M], db:Database):Route = {

    models = Set(name) ++ models

    val utils = new ModelUtils[T,M](name,table)
    import utils._
    import JsonProtocol._


    pathPrefix(name) {
      path(LongNumber) { i=>
        get {
          onComplete(getById(i)) {
            case Success(entity) => complete(entity)
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        } ~
        put {
          entity(as[M]) { e =>
            val result = db.run{ table.insertOrUpdate(e) }.map(_ => e)
            complete(result) //result should be in the same future as e
          }
        } ~
        delete {
          onComplete(deleteById(i)) {
            case Success(affectedRow) => complete(JSONCount(affectedRow))
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      } ~
      path("schema") {
        get {
          complete{ JSONSchemas.of(name,db) }
        }
      } ~
      path("form") {
        get {
          complete{ JSONForm.of(name,db) }
        }
      } ~
      path("keys") {
        get {
          complete{ JSONSchemas.keysOf(name) }
        }
      } ~
      path("count") {
        get { ctx =>
          db.run{table.length.result}.map{ result =>
            ctx.complete{ JObject(List(JField("count",JInt(result)))) }
          }
        }
      } ~
      path("list") {
        post {
          entity(as[JSONQuery]) { query =>
            println("list")
            complete(find(query))
          }
        }
      } ~
      pathEnd{
        get {
          val result:Future[Seq[T#TableElementType]] = db.run{table.take(50).result}
          onComplete(result) {
            case Success(results) => complete(results)
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        } ~
        post {
          entity(as[M]) { e =>
            val result = db.run { table.forceInsert(e) }.map(_ => e)
            complete(result)
          }
        }
      }
    }
  }

}

