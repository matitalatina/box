package ch.wsl.box.client.routes

import ch.wsl.box.client.controllers.{Controller, Container, TableController}
import ch.wsl.box.client.model.Menu
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object AppRouter {


  val tableController = new TableController
  val homeController = new Controller {

    override def leftMenu: Future[Vector[String]] = Future{
      Vector("Tables")
    }

    override def leftMenuTitle: String = "Main"

    override def leftMenuClick(e: String): Callback = e match {
      case "Tables" => routeTo(tableController.homeContainer)
      case _ => routeTo(homeContainer)
    }
    override def topMenuClick(m:Menu): Callback = routeTo(homeContainer)

  }





    val config = RouterConfigDsl[Container].buildConfig{ dsl =>
      import dsl._

      ( trimSlashes
      | staticRoute("",homeController.homeContainer) ~> renderR(RoutesUtils.renderController(homeController,homeController.homeContainer))
      | tableController.containers.routes.prefixPath_/("#tables")
      ).notFound(redirectToPage(homeController.homeContainer)(Redirect.Replace))
      .renderWith(layout)


    }





  def layout(c: RouterCtl[Container], r: Resolution[Container]) = {
      <.div( //fix for Material Design Light
        r.render()
      )
  }

  val baseUrl = BaseUrl.fromWindowOrigin

  val router = Router(baseUrl, config)

}
