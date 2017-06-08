package ch.wsl.box.client.views.components

import ch.wsl.box.client.{IndexState, LoginState, RoutingState}
import ch.wsl.box.client.styles.GlobalStyles
import org.scalajs.dom.raw.Element

import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import ch.wsl.box.client.Context._
import ch.wsl.box.client.utils.Session
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.dropdown.UdashDropdown
import io.udash.bootstrap.navs.{UdashNav, UdashNavbar}
import io.udash.properties.seq.SeqProperty
import io.udash._
import org.scalajs.dom.Event

case class MenuLink(name:String, state:RoutingState)

object Header {

  import ch.wsl.box.client.Context

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
        },
        if(Session.isLogged()) {
          a(onclick :+= ((e:Event) => Session.logout() ),"Logout")
        } else frag()
      )
    ).render
  }
}