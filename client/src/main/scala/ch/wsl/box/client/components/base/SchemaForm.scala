package ch.wsl.box.client.components.base

import ch.wsl.box.client.components.base.widget.Widget
import ch.wsl.box.client.css.CommonStyles
import ch.wsl.box.client.utils.Log
import ch.wsl.box.model.shared.JSONSchemaUI
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.Attr.Ref
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.raw.HTMLInputElement

import scala.scalajs.js
import scala.scalajs.js.{Any, UndefOr}

/**
  * Created by andreaminetti on 24/02/16.
  */
object SchemaForm {


  case class Props(schema:Option[String], ui:Option[JSONSchemaUI], onSubmit: SchemaFormNative.State => Unit, formData:Option[js.Any] = None)


//  val debugRefKey = "debugRef"
//  val debugRef = Ref.to(Debug.component,debugRefKey)

  class Backend($: BackendScope[Props, Unit]) {

    def onChange(formState:SchemaFormNative.State):Unit = {
    //  debugRef($).foreach(_.backend.change(Some(formState.formData)))
    }

    def remount() = Widget.mount


    def render(P:Props) = {
      val sfProps = for{
        schema <- P.schema
      } yield SchemaFormNative.Props(schema,P.ui,onSubmit = Some(P.onSubmit),formData = P.formData, onChange = Some(onChange), widgets = Some(Widget()))

      sfProps match {
        case Some(sfp) => renderForm(sfp)
        case None => renderNone()
      }

    }

    def renderNone() = {
      <.div(CommonStyles.card,
        ""
      )
    }

    def renderForm(P:SchemaFormNative.Props) = {
      <.div(CommonStyles.card,
        SchemaFormNative(P),
        <.hr(),
        //,Debug(ref=debugRefKey)
        <.button(^.onClick --> remount(),"REMOUNT")
      )
    }
  }

  val component = ScalaComponent.build[Props]("SchemaForm")
    .renderBackend[Backend]
    .componentDidMount( p => Widget.mount)
    .build


  def apply(props: Props) = component(props)

}
