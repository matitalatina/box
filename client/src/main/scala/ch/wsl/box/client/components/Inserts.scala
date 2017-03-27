package ch.wsl.box.client.components

import ch.wsl.box.client.components.base.{SchemaForm, SchemaFormNative}
import ch.wsl.box.client.controllers.CRUDController
import ch.wsl.box.client.css.CommonStyles
import ch.wsl.box.model.shared.JSONSchemaUI
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ScalaComponent}
import japgolly.scalajs.react.vdom.html_<^._

import scala.concurrent.ExecutionContext.Implicits.global


case class Inserts(controller:CRUDController) {


  case class State(schema:Option[String], ui:Option[JSONSchemaUI])

  val initialState = State(None,None)

  class Backend(scope:BackendScope[Unit,State]) {

    val state = for{
      schema <- controller.schemaAsString
      form <- controller.uiSchema
    } yield scope.setState(State(schema = Some(schema), ui = Some(form))).runNow()

    def onSubmit(s:SchemaFormNative.State):Unit = {
      Callback.log("Inserting") >>
      controller.onInsert(s.formData) >>
      controller.routeTo(controller.listContainer)
    }.runNow()

    def render(s:State) = {
      if(s.schema.isDefined) {

        println(s.ui)

        <.div(CommonStyles.row,
          <.div(CommonStyles.fullWidth, SchemaForm(SchemaForm.Props(s.schema, s.ui, onSubmit)))
        )
      } else <.div("Loading")
    }

  }

  val component = ScalaComponent.build[Unit]("ItemsInfo")
    .initialState{initialState}
    .backend(s => new Backend(s))
    .renderBackend
    .componentDidMount { p =>
      val state = for{
        schema <- controller.schemaAsString
        form <- controller.uiSchema
      } yield p.setState(State(schema = Some(schema), ui = Some(form)))
      Callback.future(state)
    }
    .build

  def apply() = component()
}
