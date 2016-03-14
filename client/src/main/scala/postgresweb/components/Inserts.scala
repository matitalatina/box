package postgresweb.components

import ch.wsl.jsonmodels.JSONSchemaUI
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import postgresweb.components.base.{SchemaForm, SchemaFormState}
import postgresweb.css.CommonStyles
import postgresweb.services.ModelClient

import scala.concurrent.ExecutionContext.Implicits.global


case class Inserts(model:String) {


  case class State(schema:String, ui:JSONSchemaUI)

  class Backend(scope:BackendScope[Unit,State],onSubmitCallback:Callback) {

    val client = ModelClient(model)

    for{
      schema <- client.schema
      form <- client.form
    } yield {
      scope.modState(_.copy(schema = schema, ui = JSONSchemaUI.fromJSONFields(form))).runNow()
    }

    def onSubmit(s:SchemaFormState):Unit = {
      scala.scalajs.js.Dynamic.global.console.log(s.formData)
      client.insert(s.formData)
      onSubmitCallback.runNow()
    }

    def render(s:State) = {
      <.div(CommonStyles.row,
        <.div(CommonStyles.fullWidth,SchemaForm(SchemaForm.Props(s.schema,s.ui,onSubmit)))
      )
    }

  }



  def component(onSubmitCallback:Callback) = ReactComponentB[Unit]("ItemsInfo")
    .initialState(State("{}",JSONSchemaUI.empty))
    .backend(s => new Backend(s,onSubmitCallback))
    .renderBackend
    .buildU

  def apply(onSubmitCallback:Callback) = component(onSubmitCallback)()
}
