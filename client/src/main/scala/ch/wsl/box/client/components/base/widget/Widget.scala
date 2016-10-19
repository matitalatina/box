package ch.wsl.box.client.components.base.widget


import ch.wsl.box.client.widgets.Register
import ch.wsl.box.model.shared.JSONSchema
import japgolly.scalajs.react._

import scala.scalajs.js
import js.JSConverters._

/**
  * Created by andreaminetti on 06/04/16.
  */
trait Widget {

  def name:String

  def render:(WidgetProps => ReactElement)

  /**
    * After render operations for widgets, usually called on custom class for widget
    *
    * @return
    */
  def mount:Callback


}

@js.native
trait WidgetProps extends js.Object{
  def schema:js.Any
  def value:js.UndefOr[js.Any]
  def defaultValue:js.UndefOr[js.Any]
  def required:Boolean
  def onChange(v:js.Any):js.Function
  def placeholder:js.UndefOr[js.Any]
  def options:js.Any
}

object Widget {


  def apply() = {
    val registred:js.Dictionary[js.Function] = js.Dictionary()
    Register().foreach{ widget =>
      registred.update(widget.name,widget.render)
    }
    registred
  }

  def mount:Callback = Register().map(_.mount).foldLeft(Callback.log("Widgets callbacks"))((i,o) => i >> o)


}