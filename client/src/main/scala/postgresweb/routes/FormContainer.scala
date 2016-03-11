package postgresweb.routes

import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.extra.router.RouterConfigDsl
import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import postgresweb.components.base.formBuilder.FormBuilderComponent
import postgresweb.components.items._
import postgresweb.services.GlobalState

//import scalajsreact.template.pages.ItemsPage

sealed abstract class FormContainer(val title: String,
                                    val routerPath: String,
                                    val model:String,
                                    val render: () => ReactElement)


object FormContainer {

  case object Home extends FormContainer("Home","home","home", () => HomePage())

  case object FormBuilder extends FormContainer("Home","home","home", () => FormBuilderComponent())


  case class Export(override val model:String) extends FormContainer("Export","export",model, () => Item2Data())
  case class Table(override val model:String) extends FormContainer("Table","table",model, () => Tables(model)())
  case class Insert(override val model:String) extends FormContainer("Insert","insert",model, () => Inserts(model)())



  case class Update(override val model:String,id:String) extends FormContainer("Update","update",model, () => Updates(model)()) {
    GlobalState.selectedId = Some(id)
  }
  object Update {
    def withGlobal(model: String): Update = Update(model, GlobalState.selectedId.getOrElse("none"))
  }


  private val menu:Vector[() => FormContainer] = Vector(() => Home)

  private def modelMenu(model:String):Vector[() => FormContainer] = Vector(() => Table(model),() => Insert(model),() => Update.withGlobal(model),() => Export(model))

  val routes = RouterConfigDsl[FormContainer].buildRule { dsl =>
    import dsl._

    val table:RouteB[Table] = (string("^[a-z0-9_-]+") / "table").caseClass[Table]
    val insert:RouteB[Insert] = (string("^[a-z0-9_-]+") / "insert").caseClass[Insert]
    val update:RouteB[Update] = (string("^[a-z0-9_-]+") / "update" / string("(.+)$")).caseClass[Update]



    ( staticRoute("home",Home) ~> renderR(r => WindowComponent(props = WindowComponent.Props(menu,Home, r)))
    | staticRoute("formBuilder",FormBuilder) ~> renderR(r => WindowComponent(props = WindowComponent.Props(menu,FormBuilder, r)))
    | dynamicRouteCT(table) ~> dynRenderR{ case(m,r) => WindowComponent(props = WindowComponent.Props(modelMenu(m.model),m, r))}
    | dynamicRouteCT(insert) ~> dynRenderR{ case(m,r) => WindowComponent(props = WindowComponent.Props(modelMenu(m.model),m, r))}
    | dynamicRouteCT(update) ~> dynRenderR{ case(m,r) => WindowComponent(props = WindowComponent.Props(modelMenu(m.model),m, r))}
    )

  }

}
