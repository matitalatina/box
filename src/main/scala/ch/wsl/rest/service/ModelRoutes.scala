package ch.wsl.rest.service

import ch.wsl.rest.domain.{UglyDBFilters, JSONQuery, JSONForm, JSONSchema}
import net.liftweb.json.JsonAST._
import slick.lifted.ColumnOrdered
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import spray.routing.PathMatchers.IntNumber
import spray.routing._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.PostgresDriver.api._

import ch.wsl.rest.domain.EnhancedTable._

/**
 * Created by andreaminetti on 16/02/16.
 */
trait ModelRoutes extends HttpService with UglyDBFilters {

  var models = Set[String]()




  def model[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T])(implicit mar:Marshaller[M], unmar: Unmarshaller[M], db:Database):Route = {

    case class JSONResult(count:Int,data:Seq[M])

    models = Set(name) ++ models

    import JsonProtocol._

    def getById(i:Int):Future[T#TableElementType] = {
      def fil(pk:String):Rep[Seq[T#TableElementType]] =  table.filter(x => super.==(x.col(pk),i)).take(1)

      for{
        pks <- JSONSchema.keysOf(name,db)
        result <- db.run{ fil(pks.head).result }
      } yield result.head
    }

    def find(query:JSONQuery):Future[JSONResult] = {
      val qFiltered = query.filter.foldRight[Query[T,M,Seq]](table){case ((field,jsFilter),query) =>
        println(jsFilter)
        query.filter(x => operator(jsFilter.operator.getOrElse("="))(x.col(field),jsFilter.value))
      }

      val qSorted = query.sorting.foldRight[Query[T,M,Seq]](qFiltered){case ((field,dir),query) =>
        query.sortBy{ x =>
          val c:slick.driver.PostgresDriver.api.Rep[_] = x.col(field)
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
      } yield JSONResult(count,result)
    }


    pathPrefix(name) {
      path(IntNumber) { i=>
        get {
          complete{getById(i)}
        } ~
          put {
            entity(as[M]) { e =>
              val result = db.run{ table.insertOrUpdate(e) }.map(_ => e)
              complete(result) //result should be in the same future as e
            }
          } ~
          post {
            entity(as[M]) { e =>
              val result = db.run{ table.forceInsert(e) }.map(_ => e)
              complete(result)
            }
          }

      } ~
        path("schema") {
          get {
            complete{ JSONSchema.of(name,db) }
          }
        } ~
        path("form") {
          get {
            complete{ JSONForm.of(name,db) }
          }
        } ~
        path("keys") {
          get {
            complete{ JSONSchema.keysOf(name,db) }
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
          get { ctx =>
            ctx.complete {
              val q:Rep[Seq[T#TableElementType]] = table.take(50)
              val result: Future[Seq[M]] = db.run{ q.result }
              result
            }
          } ~
            post {
              entity(as[M]) { e =>
                val result = db.run { table.forceInsert(e) }.map(_ => e)
                complete(result)
              }
            } ~
            put {
              entity(as[M]) { e =>
                val result = db.run { table.insertOrUpdate(e) }.map(_ => e)
                complete(result)
              }
            }
        }
    }
  }

}
