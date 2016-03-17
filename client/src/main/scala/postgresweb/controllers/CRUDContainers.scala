package postgresweb.controllers

import japgolly.scalajs.react.ReactComponentU
import japgolly.scalajs.react.extra.router.RouterConfigDsl
import postgresweb.components.base.formBuilder.FormBuilderComponent
import postgresweb.components._
import postgresweb.model.Menu
import postgresweb.routes.RoutesUtils

/**
  * Created by andreaminetti on 15/03/16.
  */
class CRUDContainers(controller:CRUDController) {

  //case class Export(override val model:String) extends Container("Export","export",model, () => Item2Data())
  case class Table(override val model:String) extends Container("Table",model,Tables(controller)())
  case class Insert(override val model:String) extends Container("Insert",model,Inserts(controller)())
  case class Update(override val model:String,id:String) extends Container("Update",model,Updates(controller)())


  case object CRUDHome extends Container("Home","none",CRUDHomes(controller)())

  val menu:Vector[Menu] =
    Vector(
      Menu("Table",(m,_) => Table(m)),
      Menu("Insert",(m,_) => Insert(m)),
      Menu("Update",(m,i) => Update(m,i))
    )

  val routes = RouterConfigDsl[Container].buildRule { dsl =>
    import dsl._


    val table = dynamicRouteCT[Table](( string("^[a-z0-9_-]+") / "table").caseClass[Table])
    val insert = dynamicRouteCT[Insert](( string("^[a-z0-9_-]+") / "insert").caseClass[Insert])
    val update = dynamicRouteCT[Update](( string("^[a-z0-9_-]+") / "update" / string("(.+)$")).caseClass[Update])


    (
      staticRoute("",CRUDHome) ~> renderR(r => RoutesUtils.renderController(controller,CRUDHome)(r))
    | table ~> dynRenderR { case (m, r) => RoutesUtils.renderControllerWithModel(controller)(r, m) }
    | insert ~> dynRenderR { case (m, r) => RoutesUtils.renderControllerWithModel(controller)(r, m) }
    | update ~> dynRenderR { case (m, r) => RoutesUtils.renderControllerWithModel(controller)(r, m) }
    )
  }

}

object Containers{

  case object Home extends Container("Home","home",HomePage())
  case object FormBuilder extends Container("FormBuilder","none",FormBuilderComponent())



}


sealed abstract class Container(val title: String,
                                val model:String,
                                val component:ReactComponentU[_,_,_,_]
                               )

