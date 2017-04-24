package ch.wsl.box.client.views.components

import ch.wsl.box.client.{IndexState, RoutingState}
import ch.wsl.box.client.config.ExternalUrls
import ch.wsl.box.client.styles.GlobalStyles
import org.scalajs.dom.raw.Element

import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import ch.wsl.box.client.Context._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.dropdown.UdashDropdown
import io.udash.bootstrap.navs.{UdashNav, UdashNavbar}
import io.udash.properties.seq.SeqProperty

case class MenuLink(name:String, state:RoutingState)

object Header {

  private def linkFactory(l: MenuLink) =
    a(href := l.state.url)(span(l.name)).render

  def navbar(title:String, links:Seq[MenuLink]) = {
    header(
      div(BootstrapStyles.pullLeft)(b(title)),
      div(BootstrapStyles.pullRight) (
        links.map{link =>
          a(href := link.state.url)(
            link.name
          )
        }
      )
    ).render
  }
}