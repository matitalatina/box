package postgresweb.routes

import japgolly.scalajs.react.{ReactComponentB, ReactComponentU}
import japgolly.scalajs.react.extra.router.StaticDsl.{DynamicRouteB, RouteB}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.components.base.formBuilder.FormBuilderComponent
import postgresweb.components._
import postgresweb.controllers.{Controller, TableController}




sealed abstract class Container(val title: String,
                                val model:String,
                                val component:ReactComponentU[_,_,_,_]
                                                 )



object AppRouter {



  val tableController = new TableController
  val homeController = new Controller {}

  case object Home extends Container("Home","home",HomePage())
  case object FormBuilder extends Container("FormBuilder","none",FormBuilderComponent())
  //case class Export(override val model:String) extends Container("Export","export",model, () => Item2Data())
  case class Table(override val model:String) extends Container("Table",model,Tables(tableController)())
  case class Insert(override val model:String) extends Container("Insert",model,Inserts(tableController)())
  case class Update(override val model:String,id:String) extends Container("Update",model,Updates(tableController)())


    val routes = RouterConfigDsl[Container].buildRule { dsl =>
      import dsl._


      val table = dynamicRouteCT[Table](( string("^[a-z0-9_-]+") / "table").caseClass[Table])
      val insert = dynamicRouteCT[Insert](( string("^[a-z0-9_-]+") / "insert").caseClass[Insert])
      val update = dynamicRouteCT[Update](( string("^[a-z0-9_-]+") / "update" / string("(.+)$")).caseClass[Update])


      (
        table ~> dynRenderR { case (m, r) => RoutesUtils.renderControllerWithModel(tableController)(r, m) }
      | insert ~> dynRenderR { case (m, r) => RoutesUtils.renderControllerWithModel(tableController)(r, m) }
      | update ~> dynRenderR { case (m, r) => RoutesUtils.renderControllerWithModel(tableController)(r, m) }
      )
    }

    val config = RouterConfigDsl[Container].buildConfig{ dsl =>
      import dsl._

      ( trimSlashes
      | staticRoute("",Home) ~> renderR(RoutesUtils.renderController(homeController,Home))
      | routes.prefixPath_/("#tables")
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
