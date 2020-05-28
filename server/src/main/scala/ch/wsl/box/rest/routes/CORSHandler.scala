package ch.wsl.box.rest.routes

import akka.http.scaladsl.model.{HttpHeader, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.{HttpOrigin, HttpOriginRange, Origin, ResponseHeader, `Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`, `Access-Control-Expose-Headers`}
import akka.http.scaladsl.server._

object CORSHandler {
  def all = new CORSHandler("no-header",Seq("*"))
}

class CORSHandler(authHeaderName:String,_origins:Seq[String]) {
  import Directives._
  import akka.http.scaladsl.model.HttpMethods._

  val origins = _origins.map(o => HttpOrigin(o))

  private def corsResponseHeaders(origin:HttpOrigin):List[HttpHeader] = {
    val origs = origins.contains(origin) match {
      case true => `Access-Control-Allow-Origin`(origin)
      case false => `Access-Control-Allow-Origin`.forRange(HttpOriginRange(origins:_*))
    }
    List(
      origs,
      `Access-Control-Allow-Credentials`(true),
      `Access-Control-Allow-Headers`("Content-Type",authHeaderName,"cache-control"),
      `Access-Control-Expose-Headers`(authHeaderName),
      `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)
    )
  }

  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    optionalHeaderValueByType[Origin](()).flatMap {
      case None => pass
      case Some(origin) => respondWithHeaders(corsResponseHeaders(origin.origins.head))
    }
  }

  //this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK))
  }

  // Wrap the Route with this method to enable adding of CORS headers
  def handle(r: Route): Route = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }

  // Helper method to add CORS headers to HttpResponse
  // preventing duplication of CORS headers across code
//  def addCORSHeaders(response: HttpResponse):HttpResponse =
//    response.withHeaders(corsResponseHeaders)

}