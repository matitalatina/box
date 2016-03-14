package postgresweb.routes

import japgolly.scalajs.react.ReactComponentU
import japgolly.scalajs.react.extra.router.StaticDsl.{DynamicRouteB, RouteB}
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.components.base.formBuilder.FormBuilderComponent
import postgresweb.components.{HomePage, Tables, Updates, WindowComponent}
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
  case class Table(override val model:String) extends Container("Table",model,Tables(model,tableController)())
  //case class Insert(override val model:String) extends Container("Insert","insert",model, () => Inserts(model)())
  case class Update(override val model:String,id:String) extends Container("Update",model,Updates(model)())


    val config = RouterConfigDsl[Container].buildConfig{ dsl =>
      import dsl._



      val table   =  dynamicRouteCT[Table]((string("^[a-z0-9_-]+") / "table").caseClass[Table])
      val update  =  dynamicRouteCT[Update]((string("^[a-z0-9_-]+") / "update" / string("(.+)$")).caseClass[Update])


      ( trimSlashes
        | staticRoute("", Home) ~> renderR { RoutesUtils.renderController(homeController,Home) }
        | table ~> dynRenderR{ case (m,r) => RoutesUtils.renderControllerWithModel(tableController)(r,m) }
        | update ~> dynRenderR{ case (m,r) => RoutesUtils.renderControllerWithModel(tableController)(r,m) }
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
