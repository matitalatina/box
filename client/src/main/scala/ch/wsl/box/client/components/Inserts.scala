package ch.wsl.box.client.components

import ch.wsl.box.client.components.base.{SchemaForm, SchemaFormState}
import ch.wsl.box.client.controllers.CRUDController
import ch.wsl.box.client.css.CommonStyles
import ch.wsl.box.model.shared.JSONSchemaUI
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}

import scala.concurrent.ExecutionContext.Implicits.global


case class Inserts(controller:CRUDController) {


  case class State(schema:String, ui:JSONSchemaUI)

  val initialState = State("{}",JSONSchemaUI.empty)

  class Backend(scope:BackendScope[Unit,State]) {


    for{
      schema <- controller.schemaAsString
      form <- controller.uiSchema
    } yield {
      scope.modState(_.copy(schema = schema, ui = form)).runNow()
    }

    def onSubmit(s:SchemaFormState):Unit = {
      Callback.log("Inserting") >>
      controller.onInsert(s.formData) >>
      controller.routeTo(controller.listContainer)
    }.runNow()

    def render(s:State) = {
      if(s != initialState) {
        <.div(CommonStyles.row,
          <.div(CommonStyles.fullWidth, SchemaForm(SchemaForm.Props(s.schema, s.ui, onSubmit)))
        )
      } else <.div()
    }

  }



  val component = ReactComponentB[Unit]("ItemsInfo")
    .initialState(initialState)
    .backend(s => new Backend(s))
    .renderBackend
    .buildU

  def apply() = component()
}
