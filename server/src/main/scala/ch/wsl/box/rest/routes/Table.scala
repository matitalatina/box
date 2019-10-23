package ch.wsl.box.rest.routes

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.headers.{ContentDispositionTypes, `Content-Disposition`}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromRequestUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.model.shared.{JSONCount, JSONData, JSONID, JSONQuery}
import ch.wsl.box.rest.logic.{DbActions, JSONTableActions}
import ch.wsl.box.rest.utils.{BoxConf, JSONSupport, UserProfile}
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}
import com.typesafe.config.{Config, ConfigFactory}
import scribe.Logging
import slick.lifted.TableQuery
import ch.wsl.box.rest.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.EntityMetadataFactory
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


/**
 * Created by andreaminetti on 16/02/16.
 */

object Table {

  var tables = Set[String]()
  var boxTables = Set[String]()

}

case class Table[T <: ch.wsl.box.rest.jdbc.PostgresProfile.api.Table[M],M <: Product](name:String, table:TableQuery[T], lang:String="en", isBoxTable:Boolean = false)
                                                            (implicit
                                                             enc: Encoder[M],
                                                             dec:Decoder[M],
                                                             mat:Materializer,
                                                             up:UserProfile,
                                                             ec: ExecutionContext) extends enablers.CSVDownload with Logging {


  import JSONSupport._
  import akka.http.scaladsl.model._
  import akka.http.scaladsl.server.Directives._
  import ch.wsl.box.shared.utils.Formatters._
  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.JSONUtils._
  import ch.wsl.box.model.shared.EntityKind

    implicit val db = up.db

//    println(s"adding table: $name" )
    isBoxTable match{
      case false => Table.tables = Set(name) ++ Table.tables
      case true => Table.boxTables = Set(name) ++ Table.boxTables
    }

//    val jsonActions= new JsonTableActions(table)
    val dbActions = new DbActions[T,M](table)
    val jsonActions = JSONTableActions[T,M](table)
    val limitLookupFromFk: Int = BoxConf.limitLookupFromFk

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
                  onComplete(dbActions.getById(id)) {
                    case Success(data) => {
                      complete(data)
                    }
                    case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                  }
                } ~
                  put {
                    entity(as[M]) { e =>
                      onComplete(dbActions.updateIfNeededById(id, e)) {
                        case Success(entity) => complete(e)
                        case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                      }
                    }
                  } ~
                  delete {
                    onComplete(dbActions.deleteById(id)) {
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
          complete{ EntityMetadataFactory.of(name, lang, limitLookupFromFk) }   //can set "en" hardcoded, since base table JSONForm do not change with language
        }
      } ~
      path("keys") {   //returns key fields names
        get {
          complete{ EntityMetadataFactory.keysOf(name) }
        }
      } ~
      path("ids") {   //returns all id values in JSONKeys format filtered according to specified JSONQuery (as body of the post)
        post {
          entity(as[JSONQuery]) { query =>
            complete {
              jsonActions.ids(query)
//              EntityActionsRegistry().tableActions(name).ids(query)
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
            complete(dbActions.find(query))
          }
        }
      } ~
      path("csv") {           //all values in csv format according to JSONQuery
          post {
            entity(as[JSONQuery]) { query =>
              logger.info("csv")
              complete(Source.fromPublisher(dbActions.findStreamed(query).mapResult(x => CSV.writeRow(x.values()))).log("csv"))
            }
          } ~
          respondWithHeader(`Content-Disposition`(ContentDispositionTypes.attachment,Map("filename" -> s"$name.csv"))) {
            get {
              parameters('q) { q =>
                val query = parse(q).right.get.as[JSONQuery].right.get
                val csv = Source.fromFuture(EntityMetadataFactory.of(name,lang, limitLookupFromFk).map{ metadata =>
                  CSV.writeRow(metadata.fields.map(_.name))
                }).concat(Source.fromPublisher(dbActions.findStreamed(query)).map(x => CSV.writeRow(x.values()))).log("csv")
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
