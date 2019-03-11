package ch.wsl.box.client.views.components

import ch.wsl.box.client.{IndexState, LoginState, RoutingState}
import ch.wsl.box.client.styles.GlobalStyles
import org.scalajs.dom.raw.Element
import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import io.udash.css.CssView._
import ch.wsl.box.client.Context._
import ch.wsl.box.client.services.Navigate
import ch.wsl.box.client.utils.{ClientConf, Labels, Session, UI}
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
    frag(a(ClientConf.style.linkHeaderFooter,onclick :+= ((e:Event) => { showMenu.set(false); Navigate.to(link.state)} ))(
      link.name
    )," ")
  }

  def uiMenu = UI.menu.map{ link =>
    frag(a(ClientConf.style.linkHeaderFooter,onclick :+= ((e:Event) => {showMenu.set(false); Navigate.toUrl(link.url)} ))(
      Labels(link.name)
    )," ")
  }

  def otherMenu:Seq[generic.Frag[Element, Node]] = Seq(
    if(Session.isLogged()) {
      frag(a(ClientConf.style.linkHeaderFooter,onclick :+= ((e:Event) => { showMenu.set(false); Session.logout() } ),"Logout")," ")
    } else frag(),
    "  ",
    Labels.header.lang + " ",
    ClientConf.langs.map{ l =>
      span(a(ClientConf.style.linkHeaderFooter,onclick :+= ((e:Event) => { showMenu.set(false); Session.setLang(l)  } ),l)," ")
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
      div(BootstrapStyles.pullLeft)(b(title), small(ClientConf.style.noMobile,user.map("   -   " + _))),
      div(BootstrapStyles.pullRight,ClientConf.style.noMobile) (
        menu(links)
      ),
      div(BootstrapStyles.pullRight,ClientConf.style.mobileOnly)(
        a(ClientConf.style.linkHeaderFooter,
          produce(showMenu){ if(_) span("❌").render else span("☰").render},
          onclick :+= ((e:Event) => showMenu.set(!showMenu.get))
        )
      ),
      showIf(showMenu) {
        div(ClientConf.style.mobileMenu)(
          (menuLinks(links) ++ uiMenu).map(frag(_,br)),hr,
          user.map(frag(_,br)),otherMenu.map(frag(_,br)),hr,
          Footer.copyright
        ).render
      }
    ).render
  }
}