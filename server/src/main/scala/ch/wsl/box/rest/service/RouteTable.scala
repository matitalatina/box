package ch.wsl.box.rest.service

import ch.wsl.box.model.shared.{JSONQuery, JSONCount}
import ch.wsl.box.rest.logic._
import org.json4s.JsonAST._
import slick.driver.PostgresDriver.api._
import spray.http.StatusCodes
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling._
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by andreaminetti on 16/02/16.
 */

trait RouteTable extends HttpService {

  var models = Set[String]()

  def model[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T])(implicit mar:Marshaller[M], unmar: Unmarshaller[M], db:Database):Route = {

    models = Set(name) ++ models

    val utils = new RouteHelper[T,M](name,table)
    import JsonProtocol._


    pathPrefix(name) {
      path(LongNumber) { i=>
        get {
          onComplete(utils.getById(i)) {
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
          onComplete(utils.deleteById(i)) {
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
            complete(utils.find(query))
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

