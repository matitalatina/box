package ch.wsl.box.rest.logic.functions

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

object WSImpl extends RuntimeWS {

  import akka.http.scaladsl.model._

  private def request(req: HttpRequest)(implicit ec:ExecutionContext, mat:Materializer, system:ActorSystem): Future[String] = Http().singleRequest(req).flatMap{ response =>
    response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
      body.utf8String
    }
  }

  override def get(url: String)(implicit ec:ExecutionContext, mat:Materializer, system:ActorSystem): Future[String] = request(HttpRequest(uri = url))

  override def post(url: String, data: String, contentType:String)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem): Future[String] = {

    val contentTypeHeader:ContentType =ContentType.parse(contentType).getOrElse(ContentTypes.`text/plain(UTF-8)`)

    for{
      entity <- Marshal(data).to[RequestEntity]
      result <- request(HttpRequest(uri = url, entity = entity.withContentType(contentTypeHeader), method = HttpMethods.POST))
    } yield result
  }

}
