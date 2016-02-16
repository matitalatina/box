package ch.wsl.rest.service

import ch.wsl.rest.domain.{JSONForm, JSONSchema}
import net.liftweb.json._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import spray.routing._



import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.PostgresDriver.api._

import ch.wsl.rest.domain.EnhancedTable._

/**
 * Created by andreaminetti on 16/02/16.
 */
trait ViewRoutes extends HttpService {

  var views = Set[String]()

  def view[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T])(implicit mar:Marshaller[M], unmar: Unmarshaller[M], db:Database):Route = {

    case class JSONResult(count:Int,data:List[M])

    views = Set(name) ++ views

    import JsonProtocol._


    pathPrefix(name) {
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

            val result = db.run { table.length.result }.map{r =>
              JObject(List(JField("count",JInt(r))))
            }
            ctx.complete{ result }
          }
        } ~
        //            path("list") {
        //                post {
        //                  entity(as[JSONQuery]) { query =>
        //                    val (result,count) = db withSession { implicit s =>
        //
        //
        //
        //                        val qFiltered = query.filter.foldRight[Query[T,M,Seq]](table.tq){case ((field,jsFilter),query) =>
        //                          println(jsFilter)
        //                          query.filter(table.filter(field, operator(jsFilter.operator.getOrElse("=")), jsFilter.value))
        //                        }
        //
        //                        val qSorted = query.sorting.foldRight[Query[T,M,Seq]](qFiltered){case ((field,dir),query) =>
        //                          query.sortBy{ x =>
        //                            val c = table.columns(field)(x)
        //                            dir match {
        //                              case "asc" => c.asc
        //                              case "desc" => c.desc
        //                            }
        //                          }
        //                        }
        //
        //                        (qSorted
        //                        .drop((query.page - 1) * query.count)
        //                        .take(query.count)
        //                        .list,
        //                        qSorted.length.run)
        //
        //
        //                    }
        //
        //
        //                    complete(JSONResult(count,result))
        //                  }
        //                }
        //            } ~
        pathEnd{
          get { ctx =>
            ctx.complete {
              val q:Rep[Seq[T#TableElementType]] = table.take(50)
              val result: Future[Seq[M]] = db.run{ q.result }
              result
            }

          }
        }
    }
  }
}
