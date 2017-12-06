package ch.wsl.box.rest.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import ch.wsl.box.rest.logic.{JSONFormMetadata, LangHelper}
import ch.wsl.box.rest.model.{Conf, DBFile}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext


/**
  * Created by andreaminetti on 15/03/16.
  */
trait
Root {

  implicit val materializer:Materializer
  implicit val executionContext:ExecutionContext



  /**
    * Base of all HTTP calls
    */



  val route:Route = {

    import Directives._
    import ch.wsl.box.rest.utils.JSONSupport._
    import ch.wsl.box.rest.utils.Auth
    import Auth.PostgresAuthenticator._
    import io.circe.generic.auto._

    //Serving UI
    UI.clientFiles ~
      //Serving REST-API
      pathPrefix("api" / "v1") {
        //version 1
        //cors() {
          // enable cross domain usage
//          options {
//            complete(StatusCodes.OK)
//          } ~
            pathPrefix("labels"){
              path(Segment) { lang =>
                get{
                  complete(LangHelper(lang).translationTable)
                }
              }
            } ~
            path("conf"){
              get{
                complete(Auth.boxDB.run {
                    Conf.table.result
                  }.map { result =>
                    result.map(x => x.key -> x.value).toMap
                  }
                )
              }
            } ~
            postgresBasicAuth {  userProfile =>
              implicit val db = userProfile.db
                path("checkLogin"){    //dummy request to see if user can log in
                  get {
                    complete("Ok")
                  }
                } ~
                pathPrefix("file") {
                  FileRoutes.route
                }~
                pathPrefix("entity") {
                  pathPrefix(Segment) { lang =>
                    GeneratedRoutes()
                  }
                } ~
                  path("entities") {
                    get {
                      val alltables = Table.tables ++ View.views
                      complete(alltables.toSeq.sorted)
                    }
                  } ~
                path("tables") {
                    get {
                      complete(Table.tables.toSeq.sorted)
                    }
                  } ~
                path("views") {
                    get {
                      complete(View.views.toSeq.sorted)
                    }
                  } ~
                path("forms") {
                  get {
                    complete(JSONFormMetadata().list)
                  }
                } ~
                pathPrefix("form") {
                  pathPrefix(Segment) { lang =>
                    pathPrefix(Segment) { name =>
                      Form(name,lang)
                    }
                  }
                }
            }
        }
      //}
  }
}
