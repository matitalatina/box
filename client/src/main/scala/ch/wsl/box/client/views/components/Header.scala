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
import org.scalajs.dom
import org.scalajs.dom.{Event, Node}
import scalatags.generic

case class MenuLink(name:String, state:RoutingState)

object Header {

  import ch.wsl.box.client.Context

  private def linkFactory(l: MenuLink) =
    a(Navigate.click(l.state))(span(l.name)).render


  def menuLinks(links:Seq[MenuLink]):Seq[generic.Frag[Element, Node]] =  links.map{link =>
    frag(a(GlobalStyles.linkHeaderFooter,Navigate.click(link.state))(
      link.name
    )," ")
  }

  def uiMenu = UI.menu.map{ link =>
    frag(a(GlobalStyles.linkHeaderFooter,Navigate.click(link.url))(
      Labels(link.name)
    )," ")
  }

  def otherMenu:Seq[generic.Frag[Element, Node]] = Seq(
    if(Session.isLogged()) {
      frag(a(GlobalStyles.linkHeaderFooter,onclick :+= ((e:Event) => Session.logout() ),"Logout")," ")
    } else frag(),
    Labels.header.lang + ": ",
    Labels.langs.map{ l =>
      span(a(GlobalStyles.linkHeaderFooter,onclick :+= ((e:Event) => Session.setLang(l) ),l)," ")
    }
  )

  def menu(links:Seq[MenuLink]):Seq[generic.Frag[Element, Node]] =
    menuLinks(links) ++
    uiMenu ++
    otherMenu

  val showMenu = Property(false)

  def user = Option(dom.window.sessionStorage.getItem(Session.USER))

  def navbar(title:Option[String], links:Seq[MenuLink]) = {
    header(
      div(BootstrapStyles.pullLeft)(b(title), small(GlobalStyles.noMobile,user.map("   -   " + _))),
      div(BootstrapStyles.pullRight,GlobalStyles.noMobile) (
        menu(links)
      ),
      div(BootstrapStyles.pullRight,GlobalStyles.mobileOnly)(
        a(GlobalStyles.linkHeaderFooter,
          produce(showMenu){ if(_) span("❌").render else span("☰").render},
          onclick :+= ((e:Event) => showMenu.set(!showMenu.get))
        )
      ),
      showIf(showMenu) {
        div(GlobalStyles.mobileMenu)(
          (menuLinks(links) ++ uiMenu).map(frag(_,br)),hr,
          user.map(frag(_,br)),otherMenu.map(frag(_,br)),hr,
          Footer.copyright
        ).render
      }
    ).render
  }
}