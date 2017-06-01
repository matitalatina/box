package ch.wsl.box.rest.service

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.io.IO
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import org.slf4j.LoggerFactory

import scala.io.StdIn


object Boot extends App with RouteRoot {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val conf: Config = ConfigFactory.load().as[Config]("serve")
  val host = conf.as[String]("host")
  val port = conf.as[Int]("port")

  // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`
  val bindingFuture = Http().bindAndHandle(route, host, port)
  println(s"Server online at http://localhost:8080/\nPress Q to stop...")
  while(StdIn.readLine() != "q"){
    println()
  } // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

