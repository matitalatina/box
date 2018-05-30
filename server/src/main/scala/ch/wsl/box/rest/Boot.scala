package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import ch.wsl.box.rest.logic.JSONFormMetadataFactory
import ch.wsl.box.rest.routes.{BoxRoutes, GeneratedRoutes, Root}
import ch.wsl.box.rest.utils.Auth
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import scribe.{Level, Logger}

import scala.io.StdIn


object Boot extends App with Root {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher






  val conf: Config = ConfigFactory.load().as[Config]("serve")
  val host = conf.as[String]("host")
  val port = conf.as[Int]("port")

  Logger.update(Logger.rootName)(_.clearHandlers().withHandler(minimumLevel = Level.Info))

  //TODO need to be reworked now it's based on an hack, it call generated root to populate models
  GeneratedRoutes()(Auth.adminDB,materializer,executionContext)
  BoxRoutes()(Auth.adminDB,materializer,executionContext)

  Logger.update(Logger.rootName)(_.clearHandlers().withHandler(minimumLevel = Level.Warn))

  // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`

  implicit def handler:ExceptionHandler = BoxExceptionHandler()
  val bindingFuture = Http().bindAndHandle(route, host, port)     //attach the root route
  println(s"Server online at http://localhost:8080/\nPress q to stop, r to reset cache...")
  var read = ""
  do{       //endless loop until q in sbt console is pressed
    read = StdIn.readLine()
    read match {
      case "r" => {
        JSONFormMetadataFactory.resetCache()
        println("reset cache")
      }
      case _ => {}
    }
    println()
  } while(read != "q")// let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

