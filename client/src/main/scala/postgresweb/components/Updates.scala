package postgresweb.components

import ch.wsl.model.shared.JSONSchemaUI
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import postgresweb.components.base.{SchemaForm, SchemaFormState}
import postgresweb.controllers.CRUDController
import postgresweb.css.CommonStyles

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSON


case class Updates(controller:CRUDController) {

  case class State(schema:String, ui:JSONSchemaUI, value: Option[js.Any] = None)

  class Backend(scope:BackendScope[Unit,State]) {


    for{
      schema <- controller.schemaAsString
      form <- controller.uiSchema
      value <- controller.get
    } yield {
      scope.modState(_.copy(schema = schema, ui=form, value = Some(value))).runNow()
    }



    def onSubmit(s:SchemaFormState):Unit = {
      controller.onUpdate(s.formData)
    }



    def render(s:State) = {

      <.div(CommonStyles.row,
        <.div(CommonStyles.fullWidth,SchemaForm(SchemaForm.Props(s.schema,s.ui,onSubmit,s.value)))
      )
    }
  }



  val component = ReactComponentB[Unit]("ItemsInfo")
    .initialState(State("{}",JSONSchemaUI.empty))
    .renderBackend[Backend]
    .buildU

  def apply() = component()
}
