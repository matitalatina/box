package ch.wsl.box.rest.service

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import ch.wsl.box.model.shared.{JSONResult, JSONKeys, JSONQuery, JSONCount}
import ch.wsl.box.rest.logic._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import slick.driver.PostgresDriver.api._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by andreaminetti on 16/02/16.
 */

trait RouteTable {

  var models = Set[String]()

  def model[T <: slick.driver.PostgresDriver.api.Table[M],M](name:String, table:TableQuery[T])
                                                            (implicit
                                                             mat:Materializer,
                                                             unmarshaller: FromRequestUnmarshaller[M],
                                                             marshaller:ToResponseMarshaller[M],
                                                             seqmarshaller: ToResponseMarshaller[Seq[M]],
                                                             jsonmarshaller:ToResponseMarshaller[JSONResult[M]],
                                                             db:Database):Route = {

    models = Set(name) ++ models

    val utils = new RouteHelper[T,M](name,table)
    import JSONSupport._
    import Directives._
    import io.circe.generic.auto._


    pathPrefix(name) {
      pathPrefix("id") {
        path(Segment) { id =>
          get {
            onComplete(utils.getById(JSONKeys.fromString(id))) {
              case Success(entity) => complete(entity)
              case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
            }
          } ~
            put {
              entity(as[M]) { e =>
                println(e)
                val result = db.run {
                  val action = table.update(e)
                  println(action.statements)
                  action
                }.map(_ => e)
                complete(result) //result should be in the same future as e
              }
            } ~
            delete {
              onComplete(utils.deleteById(JSONKeys.fromString(id))) {
                case Success(affectedRow) => complete(JSONCount(affectedRow))
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
              }
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
        get {
          complete {
            db.run {
              table.length.result
            }.map { result =>
              JSONCount(result)
            }
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
