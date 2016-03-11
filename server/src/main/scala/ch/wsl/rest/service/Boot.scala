package ch.wsl.rest.service

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import ch.qos.logback.classic.Logger
import com.typesafe.config.{ConfigFactory, Config}
import org.slf4j.LoggerFactory
import spray.can.Http
import net.ceedubs.ficus.Ficus._



object Main extends App {



  implicit val system = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[MainServiceActor], name = "main-service")

  val conf: Config = ConfigFactory.load().as[Config]("serve")
  val host = conf.as[String]("host")
  val port = conf.as[Int]("port")

  IO(Http) ! Http.Bind(handler, interface = host, port = port)

  val logger = LoggerFactory.getLogger("Main");

  println(s"Started server - " + host + ":" + port)

}

