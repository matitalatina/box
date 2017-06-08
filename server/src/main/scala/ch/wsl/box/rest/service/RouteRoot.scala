package ch.wsl.box.rest.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import ch.wsl.box.model.tables._
import ch.wsl.box.rest.logic.Forms

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by andreaminetti on 15/03/16.
  */
trait RouteRoot extends RouteTable with RouteView with RouteUI with RouteForm with GeneratedRoutes {

  implicit val materializer:Materializer

  /**
    * Base of all HTTP calls
    */
  val route:Route = {

    import JSONSupport._
    import Directives._
    import ch.megard.akka.http.cors.CorsDirectives._
    import io.circe.generic.auto._
    import ch.wsl.box.rest.service.Auth.PostgresAuthenticator._


    //Serving UI
    clientFiles ~
      //Serving REST-API
      pathPrefix("api" / "v1") {
        //version 1
        cors() {
          // enable cross domain usage
          options {
            complete(StatusCodes.OK)
          } ~ postgresBasicAuth {  userProfile =>
              implicit val db = userProfile.db
                path("checkLogin"){
                  get {
                    complete("Ok")
                  }
                } ~
                path("file") {
                    post {
                      fileUpload("file") { case (metadata, byteSource) =>
                        val result = byteSource.runFold(Seq[Byte]()) { (acc, n) => acc ++ n.toSeq }.map(_.toArray).flatMap { bytea =>
                          import slick.driver.PostgresDriver.api._
                          db.run {
                            Files += Files_row(name = Some("test"), file = Some(bytea))
                          }.map(_ => "ok")
                        }
                        complete(result)
                      }
                    }
                } ~
                pathPrefix("model") {
                  pathPrefix(Segment) { lang =>
                    generatedRoutes()
                  }
                } ~
                path("models") {
                  get {
                    val allmodels = models ++ views
                    complete(allmodels.toSeq.sorted)
                  }
                } ~
                path("forms") {
                  get {
                    complete(Forms.list)
                  }
                } ~
                pathPrefix("form") {
                  pathPrefix(Segment) { lang =>
                    pathPrefix(Segment) { name =>
                      println(s"getting form:$name")
                      formRoutes(name,lang)
                    }
                  }
                }
            }
        }
      }
  }
}
