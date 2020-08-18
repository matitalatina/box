package ch.wsl.box.client.styles

import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.utils.BootstrapStyles.ResponsiveBreakpoint

/**
  * Created by andre on 5/30/2017.
  */
object BootstrapCol {
  def md(i:Int) = i match {
    case 0 => BootstrapStyles.hide
    case _ => BootstrapStyles.Grid.col(i,ResponsiveBreakpoint.Medium)
  }
}
