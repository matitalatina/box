package ch.wsl.box.rest.routes

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToResponseMarshaller}
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.{JSONCount, JSONData, JSONID, JSONQuery}
import ch.wsl.box.rest.logic.{DbActions, JSONMetadataFactory}
import ch.wsl.box.rest.utils.JSONSupport
import ch.wsl.box.shared.utils.CSV
import com.typesafe.config.{Config, ConfigFactory}
import scribe.Logging
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import com.typesafe.config._
import net.ceedubs.ficus.Ficus._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Created by andreaminetti on 16/02/16.
 */

object Table {

  var tables = Set[String]()
  var boxTables = Set[String]()

}

case class Table[T <: slick.jdbc.PostgresProfile.api.Table[M],M <: Product](name:String, table:TableQuery[T], isBoxTable:Boolean = false)
                                                            (implicit
                                                             mat:Materializer,
                                                             unmarshaller: FromRequestUnmarshaller[M],
                                                             marshaller:ToResponseMarshaller[M],
                                                             seqmarshaller: ToResponseMarshaller[Seq[M]],
                                                             jsonmarshaller:ToResponseMarshaller[JSONData[M]],
                                                             db:Database,
                                                             ec: ExecutionContext) extends enablers.CSVDownload with Logging {

//    println(s"adding table: $name" )
    isBoxTable match{
      case false => Table.tables = Set(name) ++ Table.tables
      case true => Table.boxTables = Set(name) ++ Table.boxTables
    }


    val utils = new DbActions[T,M](table)
    val limitLookupFromFk: Int = ConfigFactory.load().as[Int]("limitLookupFromFk")

    import JSONSupport._
    import akka.http.scaladsl.model._
    import akka.http.scaladsl.server.Directives._
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe.parser._
    import ch.wsl.box.shared.utils.Formatters._
    import ch.wsl.box.model.shared.EntityKind
    import JSONData._




  def route:Route = pathPrefix(name) {
        pathPrefix("id") {
          path(Segment) { strId =>
            JSONID.fromString(strId) match {
              case Some(id) =>
                get {
                  onComplete(utils.getById(id)) {
                    case Success(data) => {
                      complete(data)
                    }
                    case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                  }
                } ~
                  put {
                    entity(as[M]) { e =>
                      onComplete(utils.updateById(id, e)) {
                        case Success(entity) => complete(e)
                        case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                      }
                    }
                  } ~
                  delete {
                    onComplete(utils.deleteById(id)) {
                      case Success(affectedRow) => complete(JSONCount(affectedRow))
                      case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                    }
                  }
              case None => complete(StatusCodes.BadRequest, s"JSONID $strId not valid")
            }
        }
      } ~
      path("kind") {
        get {
          complete{EntityKind.TABLE.kind}
        }
      } ~
      path("metadata") {
        get {
          complete{ JSONMetadataFactory.of(name, "en", limitLookupFromFk) }   //can set "en" hardcoded, since base table JSONForm do not change with language
        }
      } ~
      path("keys") {   //returns key fields names
        get {
          complete{ JSONMetadataFactory.keysOf(name) }
        }
      } ~
      path("ids") {   //returns all id values in JSONKeys format filtered according to specified JSONQuery (as body of the post)
        post {
          entity(as[JSONQuery]) { query =>
            complete {
              EntityActionsRegistry().tableActions(name).ids(query)
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
            logger.info("list")
            complete(utils.find(query))
          }
        }
      } ~
      path("csv") {           //all values in csv format according to JSONQuery
          post {
            entity(as[JSONQuery]) { query =>
              logger.info("csv")
              complete(Source.fromPublisher(utils.findStreamed(query)))
            }
          } ~
          respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> s"$name.csv"))) {
            get {
              parameters('q) { q =>
                val query = parse(q).right.get.as[JSONQuery].right.get
                val csv = Source.fromFuture(JSONMetadataFactory.of(name,"en", limitLookupFromFk).map{ metadata =>
                  CSV.row(metadata.fields.map(_.name))
                }).concat(Source.fromPublisher(utils.findStreamed(query)).map(x => CSV.row(x.values())))
                complete(csv)
              }
            }
          }
      } ~
      pathEnd{      //if nothing is specified  return the first 50 rows in JSON format
        get {
          val data:Future[Seq[T#TableElementType]] = db.run{table.take(50).result}
          onComplete(data) {
            case Success(results) => complete(results)
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        } ~
        post {                            //inserts
          entity(as[M]) { e =>
            logger.info("Inserting: " + e)
            val data: Future[M] = db.run { table.returning(table) += e } //returns object with id
            complete(data)
          }
        }
      }
    }
}
