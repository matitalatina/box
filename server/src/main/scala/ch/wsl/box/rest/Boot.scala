package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import ch.wsl.box.rest.metadata.{JSONFormMetadataFactory, JSONMetadataFactory}
import ch.wsl.box.rest.routes.{BoxRoutes, GeneratedRoutes, Root}
import ch.wsl.box.rest.utils.{Auth, BoxConf, UserProfile}
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

    BoxConf.load()


    override val akkaConf: Config = BoxConf.akkaHttpSession

    val host = BoxConf.host
    val port = BoxConf.port

    Logger.root.clearHandlers().withHandler(minimumLevel = Some(BoxConf.loggerLevel)).replace()

    //TODO need to be reworked now it's based on an hack, it call generated root to populate models
    GeneratedRoutes("en")(Auth.adminUserProfile, materializer, executionContext)
    BoxRoutes()(Auth.boxUserProfile, materializer, executionContext)


    // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`

    implicit def handler: ExceptionHandler = BoxExceptionHandler()

    val binding: Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port) //attach the root route
    println(s"Server online at http://localhost:$port")



  }
}

object Boot extends App  {
  var root = Box.start()

  println("Press q to stop, r to reset cache...")

  var read = ""
  do { //endless loop until q in sbt console is pressed
    read = StdIn.readLine()
    read match {
      case "r" => {
        root.stop()
        root=Box.start
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

