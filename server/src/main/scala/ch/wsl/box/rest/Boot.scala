package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import ch.wsl.box.rest.logic.functions.RuntimeFunction
import ch.wsl.box.rest.metadata.{EntityMetadataFactory, FormMetadataFactory}
import ch.wsl.box.rest.routes.{BoxRoutes, Preloading, Root}
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.rest.utils.{Auth, BoxConf, UserProfile}
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import scribe._


import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
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

    val preloading: Future[Http.ServerBinding] = Http().bindAndHandle(Preloading.route, host, port)

    Registry.load()

    Logger.root.clearHandlers().withHandler(minimumLevel = Some(BoxConf.loggerLevel)).replace()

    //TODO need to be reworked now it's based on an hack, it call generated root to populate models
    Registry().routes("en")(Auth.adminUserProfile, materializer, executionContext)
    BoxRoutes()(Auth.boxUserProfile, materializer, executionContext)


    // `route` will be implicitly converted to `Flow` using `RouteResult.route2HandlerFlow`

    implicit def handler: ExceptionHandler = BoxExceptionHandler()

    val binding = for{
      pl <- preloading
      _ <- pl.terminate(1.seconds)
      b <- Http().bindAndHandle(route, host, port) //attach the root route
    } yield {
      println("Stopped preloading server and started box")
      b
    }


    println(s"Server online at http://localhost:$port")





  }
}

object Boot extends App  {
  Box.start()
}

