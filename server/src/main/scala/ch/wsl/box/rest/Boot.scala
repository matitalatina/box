package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import ch.wsl.box.rest.routes.{BoxExceptionHandler, BoxRoutes, Preloading, Root}
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.rest.utils.log.DbWriter
import ch.wsl.box.rest.utils.{Auth, BoxConfig}
import ch.wsl.box.rest.logic.NotificationsHandler
import ch.wsl.box.services.Services
import com.typesafe.config.Config
import scribe._
import scribe.writer.ConsoleWriter
import wvlet.airframe.Design

import ch.wsl.box.model.Migrate

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._


class Box(name:String,version:String)(implicit val executionContext: ExecutionContext, services: Services) {
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

  def stop() = {
    if(server != null) {
      server.unbind().onComplete(_ => system.terminate())
    }
  }



  def start() =  {


    BoxConfig.load(Auth.adminDB)


    val akkaConf: Config = BoxConfig.akkaHttpSession

    val host = BoxConfig.host
    val port = BoxConfig.port
    val origins = BoxConfig.origins

    implicit def handler: ExceptionHandler = BoxExceptionHandler(origins).handler()


    //val preloading: Future[Http.ServerBinding] = Http().bindAndHandle(Preloading.route, host, port)

    Registry.load()

    val loggerWriter = BoxConfig.logDB match  {
      case false => ConsoleWriter
      case true => new DbWriter(Auth.adminDB)
    }
    println(s"Logger level: ${BoxConfig.loggerLevel}")

    Logger.root.clearHandlers().withHandler(minimumLevel = Some(BoxConfig.loggerLevel), writer = loggerWriter).replace()


    //TODO need to be reworked now it's based on an hack, it call generated root to populate models
    Registry().routes("en")(Auth.adminUserProfile, materializer, executionContext)
    BoxRoutes()(Auth.adminUserProfile, materializer, executionContext)


    for{
      //pl <- preloading
      //_ <- pl.terminate(1.seconds)
      b <- Http().bindAndHandle(Root(s"$name $version",akkaConf,() => this.restart(), origins).route, host, port) //attach the root route
    } yield {
      println(
        s"""
          |===================================
          |
          |    _/_/_/      _/_/    _/      _/
          |   _/    _/  _/    _/    _/  _/
          |  _/_/_/    _/    _/      _/
          | _/    _/  _/    _/    _/  _/
          |_/_/_/      _/_/    _/      _/
          |
          |===================================
          |
          |Box server started at http://localhost:$port
          |
          |""".stripMargin)
      server = b
    }


  }
}

object Boot extends App  {

  val (name,app_version) = args.length match {
    case 2 => (args(0),args(1))
    case _ => ("Standalone","DEV")
  }


  def run(name:String,app_version:String,module:Design) {

    Migrate.all()

    val executionContext = ExecutionContext.fromExecutor(
      new java.util.concurrent.ForkJoinPool(Runtime.getRuntime.availableProcessors())
    )

    module.build[Services] { services =>
      val server = new Box(name, app_version)(executionContext, services)
      server.start()
    }
  }

  run(name,app_version,DefaultModule.injector)
}

