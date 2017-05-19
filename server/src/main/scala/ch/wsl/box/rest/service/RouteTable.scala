package ch.wsl.box.rest.service

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import ch.wsl.box.model.shared.{JSONCount, JSONKeys, JSONQuery, JSONResult}
import ch.wsl.box.rest.logic._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import slick.driver.PostgresDriver.api._
import akka.http.scaladsl.model._


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by andreaminetti on 16/02/16.
 */

trait RouteTable {

  var models = Set[String]()

  def model[T <: slick.driver.PostgresDriver.api.Table[M],M <: Product](name:String, table:TableQuery[T])
                                                            (implicit
                                                             mat:Materializer,
                                                             unmarshaller: FromRequestUnmarshaller[M],
                                                             marshaller:ToResponseMarshaller[M],
                                                             seqmarshaller: ToResponseMarshaller[Seq[M]],
                                                             jsonmarshaller:ToResponseMarshaller[JSONResult[M]],
                                                             db:Database):Route = {

    models = Set(name) ++ models

    val utils = new RouteHelper[T,M](table)
    import JSONSupport._
    import io.circe.generic.auto._

    import akka.http.scaladsl.server.Directives._

    import akka.http.scaladsl.model.headers._
    import akka.http.scaladsl.model._

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
                onComplete(utils.updateById(JSONKeys.fromString(id),e)) {
                  case Success(entity) => complete("Ok")
                  case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                }
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
          complete{ JSONForms.of(name,db,"en") }
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
      path("csv") {
        post {
          entity(as[JSONQuery]) { query =>
            println("csv")

            complete(utils.find(query).map(x => HttpEntity(ContentTypes.`text/plain(UTF-8)`,x.csv)))
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
            println("Inserting: " + e)
            val result: Future[M] = db.run { table.returning(table) += e } //returns object with id
            complete(result)
          }
        }
      }
    }
  }

}

