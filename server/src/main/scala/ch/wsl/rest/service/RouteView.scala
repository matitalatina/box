package ch.wsl.rest.service

import ch.wsl.box.model.shared.JSONQuery
import ch.wsl.rest.logic.{RouteHelper, JSONForm, JSONSchemas}
import org.json4s.JsonAST._
import slick.driver.PostgresDriver.api._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by andreaminetti on 16/02/16.
 */
trait RouteView extends HttpService {

  var views = Set[String]()

  def view[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T])(implicit mar:Marshaller[M], unmar: Unmarshaller[M], db:Database):Route = {

    views = Set(name) ++ views

    import JsonProtocol._

    val helper = new RouteHelper[T,M](name,table)

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
              complete(helper.find(query))
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
