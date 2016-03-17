package postgresweb.routes

import japgolly.scalajs.react.{Callback, ReactComponentB, ReactComponentU}
import japgolly.scalajs.react.extra.router.StaticDsl.{DynamicRouteB, RouteB}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.components.base.formBuilder.FormBuilderComponent
import postgresweb.components._
import postgresweb.controllers.{Containers, Container, Controller, TableController}
import postgresweb.model.Menu

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


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
