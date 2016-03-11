package postgresweb.components.items

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import postgresweb.components.base.{SchemaFormState, SchemaForm}
import postgresweb.css.CommonStyles
import postgresweb.models._
import postgresweb.services.ModelClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


case class Inserts(model:String) {


  case class State(schema:String, ui:JSONSchemaUI)

  class Backend(scope:BackendScope[Unit,State]) {

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
    }

    def render(s:State) = {
      <.div(CommonStyles.row,
        <.div(CommonStyles.fullWidth,SchemaForm(SchemaForm.Props(s.schema,s.ui,onSubmit)))
      )
    }

  }



  val component = ReactComponentB[Unit]("ItemsInfo")
    .initialState(State("{}",JSONSchemaUI.empty))
    .renderBackend[Backend]
    .buildU

  def apply() = component()
}
