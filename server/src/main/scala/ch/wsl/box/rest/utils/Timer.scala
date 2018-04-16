package ch.wsl.box.rest.utils

import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Timer extends Logging {

  def mesure[R](block: => R,msg:String = ""): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    logger.warn(s"Elapsed time: ${(t1 - t0) / 1000000000.0 }s $msg")

    result
  }

  def mesureFuture[R <: Future[_]](block: => R,msg:String = "")(implicit ec:ExecutionContext): R = {
    val t0 = System.nanoTime()

    def time(): Unit = {
      val t1 = System.nanoTime()
      logger.warn(s"Elapsed time: ${(t1 - t0) / 1000000000.0 }s $msg")
    }

    val result = block    // call-by-name
    result onComplete {
      case Success(_) => time()
      case Failure(t) => {
        time()
        println("An error has occured: " + t.getMessage)
      }
    }

    result
  }
}