package ch.wsl.rest.service

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import spray.can.Http

object Main extends App {

  val logger = LoggerFactory.getLogger("Main");

  implicit val system = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[MainServiceActor], name = "main-service")


  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)



  //enable exit with CTRL-D
  while (System.in.read() != -1) {}
  logger.warn("Received end-of-file on stdin. Exiting")
  IO(Http) ! Http.CloseAll
  System.exit(0)
}