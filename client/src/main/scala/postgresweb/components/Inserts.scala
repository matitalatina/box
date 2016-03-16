package postgresweb.components

import ch.wsl.model.shared.JSONSchemaUI
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import postgresweb.components.base.{SchemaForm, SchemaFormState}
import postgresweb.controllers.CRUDController
import postgresweb.css.CommonStyles
import postgresweb.services.TableClient$

import scala.concurrent.ExecutionContext.Implicits.global


case class Inserts(controller:CRUDController) {


  case class State(schema:String, ui:JSONSchemaUI)

  class Backend(scope:BackendScope[Unit,State]) {


    for{
      schema <- controller.schemaAsString
      form <- controller.uiSchema
    } yield {
      scope.modState(_.copy(schema = schema, ui = form)).runNow()
    }

    def onSubmit(s:SchemaFormState):Unit = {
      controller.onInsert(s.formData).runNow()
    }

    def render(s:State) = {
      <.div(CommonStyles.row,
        <.div(CommonStyles.fullWidth,SchemaForm(SchemaForm.Props(s.schema,s.ui,onSubmit)))
      )
    }

  }



  val component = ReactComponentB[Unit]("ItemsInfo")
    .initialState(State("{}",JSONSchemaUI.empty))
    .backend(s => new Backend(s))
    .renderBackend
    .buildU

  def apply() = component()
}
