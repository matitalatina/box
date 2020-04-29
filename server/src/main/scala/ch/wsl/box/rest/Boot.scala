package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer }
import ch.wsl.box.rest.routes.{BoxRoutes, Preloading, Root}
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.rest.utils.log.DbWriter
import ch.wsl.box.rest.utils.{Auth, BoxConf}
import com.typesafe.config.{Config}
import scribe._
import scribe.writer.ConsoleWriter

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._


class Box(implicit val executionContext: ExecutionContext) {
  private var server:Http.ServerBinding = null

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()




  def restart():Unit = {
    if(server != null) {
      server.terminate(5.seconds).map{ _ =>
        start()
      }
    }
  }



  def start() =  {

    implicit def handler: ExceptionHandler = BoxExceptionHandler()

    BoxConf.load()

    val akkaConf: Config = BoxConf.akkaHttpSession

    val host = BoxConf.host
    val port = BoxConf.port
    val origins = BoxConf.origins



    val preloading: Future[Http.ServerBinding] = Http().bindAndHandle(Preloading.route, host, port)

    Registry.load()

    val loggerWriter = BoxConf.logDB match  {
      case false => ConsoleWriter
      case true => new DbWriter(Auth.boxDB)
    }
    Logger.root.clearHandlers().withHandler(minimumLevel = Some(BoxConf.loggerLevel), writer = loggerWriter).replace()


    //TODO need to be reworked now it's based on an hack, it call generated root to populate models
    Registry().routes("en")(Auth.adminUserProfile, materializer, executionContext)
    BoxRoutes()(Auth.boxUserProfile, materializer, executionContext)


    for{
      pl <- preloading
      _ <- pl.terminate(1.seconds)
      b <- Http().bindAndHandle(Root(akkaConf,() => this.restart(), origins).route, host, port) //attach the root route
    } yield {
      println("Stopped preloading server and started box")
      server = b
    }


    println(s"Server online at http://localhost:$port")


  }
}

object Boot extends App  {

  val executionContext = ExecutionContext.fromExecutor(
    new java.util.concurrent.ForkJoinPool(Runtime.getRuntime.availableProcessors())
  )

  val server = new Box()(executionContext)

  server.start()
}

