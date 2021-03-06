package ch.wsl.box.client.mocks

import ch.wsl.box.client.services.HttpClient
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.File

import scala.concurrent.Future

class HttpClientMock extends HttpClient {
  override def post[D, R](url: String, obj: D)(implicit decoder: Decoder[R], encoder: Encoder[D]): Future[R] = throw new Exception("post not implemented")

  override def put[D, R](url: String, obj: D)(implicit decoder: Decoder[R], encoder: Encoder[D]): Future[R] = throw new Exception("put not implemented")

  override def get[T](url: String)(implicit decoder: Decoder[T]): Future[T] = throw new Exception("delete not implemented")

  override def delete[T](url: String)(implicit decoder: Decoder[T]): Future[T] = throw new Exception("delete not implemented")

  override def sendFile[T](url: String, file: File)(implicit decoder: Decoder[T]): Future[T] = throw new Exception("sendFile not implemented")

  override def setHandleAuthFailure(f: () => Unit): Unit = {}
}
