package ch.wsl.box.client.components.base.widget

import japgolly.scalajs.react.ReactElement

import scala.scalajs.js

/**
  * Created by andreaminetti on 06/04/16.
  */
trait Widget[T <: Widget.Props] {

  def name:String

  def render:(js.Dynamic => ReactElement)

}

object Widget {
  private var registred:js.Dictionary[js.Function] = js.Dictionary()

  def apply() = registred

  def register[T <: Widget.Props](widget: Widget[T]) = registred.update(widget.name,widget.render)

  trait Props

}