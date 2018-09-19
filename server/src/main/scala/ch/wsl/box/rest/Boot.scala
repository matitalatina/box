package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import ch.wsl.box.rest.logic.JSONMetadataFactory
import ch.wsl.box.rest.logic.JSONFormMetadataFactory
import

ch.wsl.box.rest.routes.{BoxRoutes, GeneratedRoutes, Root}
import ch.wsl.box.rest.utils.Auth
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import scribe._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.StdIn


object Box {
  def start() = new Root {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher



    val conf: Config = ConfigFactory.load().as[Config]("serve")
    val host = conf.as[String]("host")
    val port = conf.as[Int]("port")

    Logger.root.clearHandlers().clearModifiers().withHandler(minimumLevel = Some(Level.Info)).replace()
//    Logger.update(Logger.rootName)(_.clearHandlers().withHandler(minimumLevel = Level.Info))

    //TODO need to be reworked now it's based on an hack, it call generated root to populate models
    GeneratedRoutes()(Auth.adminDB, materializer, executionContext)
    BoxRoutes()(Auth.adminDB, materializer, executionContext)

//    Logger.update(Logger.rootName)(_.clearHandlers().withHandler(minimumLevel = Level.Warn))
    Logger.root.clearHandlers().withHandler(minimumLevel = Some(Level.Warn)).replace()

    // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`

    implicit def handler: ExceptionHandler = BoxExceptionHandler()

    val binding: Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port) //attach the root route
    logger.info(s"Server online at http://localhost:$port")


  }
}

object Boot extends App  {
  val root = Box.start()

  println("Press q to stop, r to reset cache...")

  var read = ""
  do { //endless loop until q in sbt console is pressed
    read = StdIn.readLine()
    read match {
      case "r" => {
        JSONFormMetadataFactory.resetCache()
        JSONMetadataFactory.resetCache()
        println("reset cache")
      }
      case _ => {}
    }
    println()
  } while (read != "q") // let it run until user presses return

  root.stop()

}

