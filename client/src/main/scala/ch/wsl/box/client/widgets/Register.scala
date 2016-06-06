package ch.wsl.box.client.widgets

import ch.wsl.box.client.components.base.widget.Widget

/**
  * Created by andreaminetti on 06/06/16.
  */
object Register {
  def apply():Seq[Widget] = Seq(
    Datepicker,
    Text
  )
}
