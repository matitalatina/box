package ch.wsl.box.rest.service

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import ch.wsl.box.model.shared.{JSONResult, JSONCount, JSONQuery}
import ch.wsl.box.rest.logic.{RouteHelper, JSONForm, JSONSchemas}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import slick.driver.PostgresDriver.api._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directives, Route}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by andreaminetti on 16/02/16.
 */
trait RouteView {

  var views = Set[String]()

  def view[T <: slick.driver.PostgresDriver.api.Table[M],M <: Product](name:String, table:TableQuery[T])(implicit
                                                                                              mat:Materializer,
                                                                                              unmarshaller: FromRequestUnmarshaller[M],
                                                                                              marshaller:ToResponseMarshaller[M],
                                                                                              seqmarshaller: ToResponseMarshaller[Seq[M]],
                                                                                              jsonmarshaller:ToResponseMarshaller[JSONResult[M]],
                                                                                              db:Database):Route = {

    views = Set(name) ++ views

    import JSONSupport._
    import Directives._
    import io.circe.generic.auto._

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
              JSONCount(r)
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
