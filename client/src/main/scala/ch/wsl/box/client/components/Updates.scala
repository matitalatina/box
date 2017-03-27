package ch.wsl.box.client.components

import ch.wsl.box.client.components.base.{SchemaForm, SchemaFormNative}
import ch.wsl.box.model.shared.JSONSchemaUI
import japgolly.scalajs.react.vdom.html_<^._
import ch.wsl.box.client.controllers.CRUDController
import ch.wsl.box.client.css.CommonStyles
import japgolly.scalajs.react._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSON


case class Updates(controller:CRUDController) {

  case class State(schema:Option[String], ui:Option[JSONSchemaUI], value: Option[js.Any] = None)

  val initialState = State(None,None)

  class Backend(scope:BackendScope[Unit,State]) {


    for{
      schema <- controller.schemaAsString
      form <- controller.uiSchema
      value <- controller.get
    } yield {
      scope.modState(_.copy(schema = Some(schema), ui=Some(form), value = Some(value))).runNow()
    }



    def onSubmit(s:SchemaFormNative.State):Unit = {
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



  val component = ScalaComponent.build[Unit]("ItemsInfo")
    .initialState(initialState)
    .renderBackend[Backend]
    .build

  def apply() = component()
}
