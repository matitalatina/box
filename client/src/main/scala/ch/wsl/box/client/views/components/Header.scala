package ch.wsl.box.client.views.components

import ch.wsl.box.client.{IndexState, LoginState, RoutingState}
import ch.wsl.box.client.styles.GlobalStyles
import org.scalajs.dom.raw.Element

import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import ch.wsl.box.client.Context._
import ch.wsl.box.client.services.Navigate
import ch.wsl.box.client.utils.{Labels, Session, UI}
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
    a(Navigate.click(l.state.url))(span(l.name)).render



  def navbar(logo:Option[String],title:Option[String], links:Seq[MenuLink]) = {
    header(
      div(BootstrapStyles.pullLeft)(logo.map(x => img(GlobalStyles.headerLogo,src := x)),b(title)),
      div(BootstrapStyles.pullRight) (
        links.map{link =>
          frag(a(GlobalStyles.linkHeaderFooter,Navigate.click(link.state.url))(
            link.name
          )," ")
        },
        UI.menu.map{ link =>
          frag(a(GlobalStyles.linkHeaderFooter,Navigate.click("#"+link.url))(
            link.name
          )," ")
        },
        if(Session.isLogged()) {
          frag(a(GlobalStyles.linkHeaderFooter,onclick :+= ((e:Event) => Session.logout() ),"Logout")," ")
        } else frag(),
        b(" "+Labels.header.lang+ ": "),
        Labels.langs.map{ l =>
          span(a(GlobalStyles.linkHeaderFooter,onclick :+= ((e:Event) => Session.setLang(l) ),l)," ")
        }
      )
    ).render
  }
}