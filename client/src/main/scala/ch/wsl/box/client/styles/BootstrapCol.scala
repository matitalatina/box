package ch.wsl.box.client.styles

import io.udash.bootstrap.BootstrapStyles

/**
  * Created by andre on 5/30/2017.
  */
object BootstrapCol {
  def md(i:Int) = i match {
    case 0 => BootstrapStyles.hide
    case 1 => BootstrapStyles.Grid.colMd1
    case 2 => BootstrapStyles.Grid.colMd2
    case 3 => BootstrapStyles.Grid.colMd3
    case 4 => BootstrapStyles.Grid.colMd4
    case 5 => BootstrapStyles.Grid.colMd5
    case 6 => BootstrapStyles.Grid.colMd6
    case 7 => BootstrapStyles.Grid.colMd7
    case 8 => BootstrapStyles.Grid.colMd8
    case 9 => BootstrapStyles.Grid.colMd9
    case 10 => BootstrapStyles.Grid.colMd10
    case 11 => BootstrapStyles.Grid.colMd11
    case 12 => BootstrapStyles.Grid.colMd12
  }
}
