package postgresweb.routes

import japgolly.scalajs.react.{Callback, ReactComponentB, ReactComponentU}
import japgolly.scalajs.react.extra.router.StaticDsl.{DynamicRouteB, RouteB}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.components.base.formBuilder.FormBuilderComponent
import postgresweb.components._
import postgresweb.controllers.Containers.Home
import postgresweb.controllers.{Container, Controller, TableController}
import postgresweb.model.Menu


object AppRouter {


  val tableController = new TableController
  val homeController = new Controller {

    override def entityClick(e: String): Callback = routeTo(Home)
    override def menuClick(m:Menu): Callback = routeTo(Home)

  }





    val config = RouterConfigDsl[Container].buildConfig{ dsl =>
      import dsl._

      ( trimSlashes
      | staticRoute("",Home) ~> renderR(RoutesUtils.renderController(homeController,Home))
      | tableController.containers.routes.prefixPath_/("#tables")
      ).notFound(redirectToPage(Home)(Redirect.Replace))
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
