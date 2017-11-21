package ch.wsl.box.rest.routes

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import ch.wsl.box.model.TablesRegistry
import ch.wsl.box.model.shared.{JSONCount, JSONKeys, JSONQuery, JSONResult}
import ch.wsl.box.rest.logic.{DbActions, JSONModelMetadata, JSONSchemas}
import ch.wsl.box.rest.utils.JSONSupport
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Created by andreaminetti on 16/02/16.
 */

object Table {



  var tables = Set[String]()

  def apply[T <: slick.jdbc.PostgresProfile.api.Table[M],M <: Product](name:String, table:TableQuery[T])
                                                            (implicit
                                                             mat:Materializer,
                                                             unmarshaller: FromRequestUnmarshaller[M],
                                                             marshaller:ToResponseMarshaller[M],
                                                             seqmarshaller: ToResponseMarshaller[Seq[M]],
                                                             jsonmarshaller:ToResponseMarshaller[JSONResult[M]],
                                                             db:Database,
                                                             ec: ExecutionContext):Route = {

    println(s"adding table: $name" )
    tables = Set(name) ++ tables

    val utils = new DbActions[T,M](table)
    import JSONSupport._
    import akka.http.scaladsl.model._
    import akka.http.scaladsl.server.Directives._
    import io.circe.generic.auto._

    pathPrefix(name) {
      pathPrefix("id") {
        path(Segment) { id =>
          get {
            onComplete(utils.getById(JSONKeys.fromString(id))) {
              case Success(result) => {
                complete(result)
              }
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
          complete{ JSONSchemas.of(name) }
        }
      } ~
      path("metadata") {
        get {
          complete{ JSONModelMetadata.of(name, "en") }   //can set "en" hardcoded, since base table JSONForm do not change with language
        }
      } ~
      path("keys") {   //returns key fields names
        get {
          complete{ JSONSchemas.keysOf(name) }
        }
      } ~
      path("keysList") {   //returns all id values in JSONKeys format filtered according to specified JSONQuery (as body of the post)
        post {
          entity(as[JSONQuery]) { query =>
            complete {
              TablesRegistry.actions(name).keyList(query,name)
            }
          }
        }
      } ~
      path("count") {     //nrows of table
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
      path("list") {           //all values in JSON format according to JSONQuery
        post {
          entity(as[JSONQuery]) { query =>
            println("list")
            complete(utils.find(query))
          }
        }
      } ~
      path("csv") {           //all values in csv format according to JSONQuery
        post {
          entity(as[JSONQuery]) { query =>
            println("csv")
            complete(utils.find(query).map(x => HttpEntity(ContentTypes.`text/plain(UTF-8)`,x.csv)))    //wrap to specify return type
          }
        }
      } ~
      pathEnd{      //if nothing is specified  return the first 50 rows in JSON format
        get {
          val result:Future[Seq[T#TableElementType]] = db.run{table.take(50).result}
          onComplete(result) {
            case Success(results) => complete(results)
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        } ~
        post {                            //inserts
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
