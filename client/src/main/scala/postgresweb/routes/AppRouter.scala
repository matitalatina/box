package postgresweb.routes

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.StaticDsl.{DynamicRouteB, RouteB}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.html.Anchor
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

    override def topMenuLink(m: Menu): vdom.ReactTagOf[Anchor] = routeController.link(homeContainer)

    override def leftMenuLink(e: String): vdom.ReactTagOf[Anchor] = e match {
      case "Tables" => routeController.link(tableController.homeContainer)
      case _ => routeController.link(homeContainer)
    }

    override def homeContainer: Container = Containers.Home
  }





    val config = RouterConfigDsl[Container].buildConfig{ dsl =>
      import dsl._

      ( trimSlashes
      | staticRoute("",Containers.Home) ~> renderR(RoutesUtils.renderController(homeController,Containers.Home))
      | tableController.containers.routes.prefixPath_/("#tables")
      ).notFound(redirectToPage(Containers.Home)(Redirect.Replace))
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
