package ch.wsl.box.client.components

import ch.wsl.box.client.components.base.{SchemaFormState, SchemaForm}
import ch.wsl.box.model.shared.JSONSchemaUI
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import ch.wsl.box.client.components.base.SchemaFormState
import ch.wsl.box.client.controllers.CRUDController
import ch.wsl.box.client.css.CommonStyles

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSON


case class Updates(controller:CRUDController) {

  case class State(schema:String, ui:JSONSchemaUI, value: Option[js.Any] = None)

  val initialState = State("{}",JSONSchemaUI.empty)

  class Backend(scope:BackendScope[Unit,State]) {


    for{
      schema <- controller.schemaAsString
      form <- controller.uiSchema
      value <- controller.get
    } yield {
      scope.modState(_.copy(schema = schema, ui=form, value = Some(value))).runNow()
    }



    def onSubmit(s:SchemaFormState):Unit = {
      Callback.log("Updating") >>
      controller.onUpdate(s.formData) >>
      controller.routeTo(controller.listContainer)
    }.runNow()



    def render(s:State) = {
      if(s != initialState) {
        <.div(CommonStyles.row,
          <.div(CommonStyles.fullWidth, SchemaForm(SchemaForm.Props(s.schema, s.ui, onSubmit, s.value)))
        )
      } else <.div()
    }
  }



  val component = ReactComponentB[Unit]("ItemsInfo")
    .initialState(initialState)
    .renderBackend[Backend]
    .buildU

  def apply() = component()
}
