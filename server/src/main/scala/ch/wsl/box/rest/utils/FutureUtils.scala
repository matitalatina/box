package ch.wsl.box.rest.utils

import scala.concurrent.{ExecutionContext, Future}

/**
  * https://stackoverflow.com/questions/20414500/how-to-do-sequential-execution-of-futures-in-scala
  */
object FutureUtils {
  def seqFutures[T, U](items: TraversableOnce[T])(yourfunction: T => Future[U])(implicit ec: ExecutionContext): Future[List[U]] = {
    items.foldLeft(Future.successful[List[U]](Nil)) {
      (f, item) => f.flatMap {
        x => yourfunction(item).map(_ :: x)
      }
    } map (_.reverse)
  }
}
