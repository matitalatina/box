package ch.wsl.box.client.controllers

import ch.wsl.box.client.components.base.formBuilder.FormBuilderComponent
import ch.wsl.box.client.components._
import ch.wsl.box.client.model.Menu
import ch.wsl.box.client.routes.RoutesUtils
import ch.wsl.box.model.shared.JSONKeys
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.extra.router.RouterConfigDsl
import japgolly.scalajs.react._

/**
  * Created by andreaminetti on 15/03/16.
  */


sealed abstract class Container(val title: String,
                                val model:String,
                                val component:Scala.Unmounted[_,_,_]
                               )

object Containers{

  //case object Home extends Container("Home","home",HomePage())
  case object FormBuilder extends Container("FormBuilder","none",FormBuilderComponent())

  case class Home(controller:Controller) extends Container("Home","none",Homes(controller)())

  case object Test extends Container("Test","none",HomePage())

}


class CRUDContainers(controller:CRUDController) {

  //case class Export(override val model:String) extends Container("Export","export",model, () => Item2Data())
  case class Table(override val model:String) extends Container("Table",model,Tables(controller)())
  case class Insert(override val model:String) extends Container("Insert",model,Inserts(controller)())
  case class Update(override val model:String,id:String) extends Container("Update",model,Updates(controller)()) {
    controller.selectId(JSONKeys.fromString(id))
  }


  val home = Containers.Home(controller)

  val menu:Vector[Menu] =
    Vector(
      Menu("Table",(m,_) => Table(m)),
      Menu("Insert",(m,_) => Insert(m)),
      Menu("Update",(m,i) => Update(m,i.asString))
    )

  val routes = RouterConfigDsl[Container].buildRule { dsl =>
    import dsl._


    val table = dynamicRouteCT[Table](( string("^[a-z0-9_-]+") / "table").caseClass[Table])
    val insert = dynamicRouteCT[Insert](( string("^[a-z0-9_-]+") / "insert").caseClass[Insert])
    val update = dynamicRouteCT[Update](( string("^[a-z0-9_-]+") / "update" / string("(.+)$")).caseClass[Update])

    (
      staticRoute("",home) ~> renderR(r => RoutesUtils.renderController(controller,home)(r))(x => x.vdomElement)
    | table ~> dynRenderR[Table,GenericComponent.Unmounted[_,_]] { case (m, r) => RoutesUtils.renderControllerWithModel(controller)(r, m) }(x => x.vdomElement)
    | insert ~> dynRenderR[Insert,GenericComponent.Unmounted[_,_]] { case (m, r) => RoutesUtils.renderControllerWithModel(controller)(r, m) }(x => x.vdomElement)
    | update ~> dynRenderR[Update,GenericComponent.Unmounted[_,_]] { case (m, r) => RoutesUtils.renderControllerWithModel(controller)(r, m) }(x => x.vdomElement)
    )
  }

}

