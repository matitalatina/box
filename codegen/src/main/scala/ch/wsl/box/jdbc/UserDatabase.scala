package ch.wsl.box.jdbc

import slick.basic.DatabasePublisher
import slick.dbio.{DBIOAction, NoStream, Streaming, StreamingDBIO}

import scala.concurrent.Future

trait UserDatabase{

  def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R]

  def stream[T](a: StreamingDBIO[Seq[T],T]): DatabasePublisher[T]

}
