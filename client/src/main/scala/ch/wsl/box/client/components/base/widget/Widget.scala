package ch.wsl.box.client.components.base.widget

import ch.wsl.box.model.shared.JSONSchema
import japgolly.scalajs.react.ReactElement

import scala.scalajs.js
import js.JSConverters._

/**
  * Created by andreaminetti on 06/04/16.
  */
trait Widget {

  def name:String

  def render:(WidgetProps => ReactElement)


}

@js.native
trait WidgetProps extends js.Object{
  def schema:js.Any
  def value:js.UndefOr[js.Any]
  def defaultValue:js.UndefOr[js.Any]
  def required:Boolean
  def onChange:js.Function
  def placeholder:js.UndefOr[js.Any]
  def options:js.Any
}

object Widget {
  private var registred:js.Dictionary[js.Function] = js.Dictionary()

  def apply() = registred

  def register(widget: Widget) = registred.update(widget.name,widget.render)


}