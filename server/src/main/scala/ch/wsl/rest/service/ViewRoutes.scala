package ch.wsl.rest.service

import ch.wsl.jsonmodels.{JSONQuery}
import ch.wsl.rest.domain.{JSONSchemas, JSONForm}
import org.json4s.JsonAST._
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

    views = Set(name) ++ views

    import JsonProtocol._

    val utils = new ModelUtils[T,M](name,table)
    import utils._

    path(name) {
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

            val result = db.run { table.length.result }.map{r =>
              JObject(List(JField("count",JInt(r))))
            }
            ctx.complete{ result }
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

          }
        }
    }
  }
}
