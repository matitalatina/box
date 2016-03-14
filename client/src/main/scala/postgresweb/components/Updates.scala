package postgresweb.components

import ch.wsl.jsonmodels.JSONSchemaUI
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import postgresweb.components.base.{SchemaForm, SchemaFormState}
import postgresweb.css.CommonStyles
import postgresweb.services.{GlobalState, ModelClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSON


case class Updates(model:String) {

  case class State(schema:String, ui:JSONSchemaUI, value: Option[js.Any] = None)

  class Backend(scope:BackendScope[Unit,State]) {



    val client = ModelClient(model)

    for{
      schema <- client.schema
      form <- client.form
      value <- client.get(GlobalState.selectedId.getOrElse(""))
    } yield {
      scope.modState(_.copy(schema = schema, ui = JSONSchemaUI.fromJSONFields(form), value = Some(value))).runNow()
    }



    def onSubmit(s:SchemaFormState):Unit = {
      scala.scalajs.js.Dynamic.global.console.log(s.formData)
      client.update(GlobalState.selectedId.getOrElse(""),JSON.stringify(s.formData))
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
