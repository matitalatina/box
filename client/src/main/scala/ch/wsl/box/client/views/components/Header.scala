package ch.wsl.box.client.views.components

import ch.wsl.box.client.{AdminState, DataKind, DataListState, EntitiesState, IndexState, LoginState, RoutingState}
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
import java.io

import org.scalajs.dom
import org.scalajs.dom.{Event, Node}
import scalatags.generic

case class MenuLink(name:String, state:RoutingState)

object Header {

  import ch.wsl.box.client.Context

  private val links:Seq[Modifier] = Seq(produce(Session.logged) { logged =>
    if(!logged) span().render else {
      val l = Seq(MenuLink(Labels.header.home, IndexState)) ++ {
        if (UI.enableAllTables) {
          Seq(
            MenuLink("Admin", AdminState),
            MenuLink(Labels.header.entities, EntitiesState("entity", "")),
            MenuLink(Labels.header.tables, EntitiesState("table", "")),
            MenuLink(Labels.header.views, EntitiesState("view", "")),
            MenuLink(Labels.header.forms, EntitiesState("form", "")),
            MenuLink(Labels.header.exports, DataListState(DataKind.EXPORT, "")),
            MenuLink(Labels.header.functions, DataListState(DataKind.FUNCTION, ""))
          )
        } else Seq()
      }
      menuLinks(l).render
    }
  })

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

  def otherMenu:Seq[Modifier] = Seq(
    showIf(Session.logged) {
      println(Session.logged.get)
      frag(a(ClientConf.style.linkHeaderFooter,onclick :+= ((e:Event) => { showMenu.set(false); Session.logout() } ),"Logout")," ").render
    },
    "  ",
    Labels.header.lang + " ",
    ClientConf.langs.map{ l =>
      span(a(ClientConf.style.linkHeaderFooter,onclick :+= ((e:Event) => { showMenu.set(false); Session.setLang(l)  } ),l)," ")
    }
  )

  def menu:Seq[Modifier] =
    links ++
    uiMenu ++
    otherMenu

  val showMenu = Property(false)

  def user = Option(dom.window.sessionStorage.getItem(Session.USER))

  def navbar(title:Option[String]) = produce(Session.logged) { x =>
    header(
      div(BootstrapStyles.pullLeft)(b(title), small(ClientConf.style.noMobile,user.map("   -   " + _))),
      div(BootstrapStyles.pullRight,ClientConf.style.noMobile) (
        menu
      ),
      div(BootstrapStyles.pullRight,ClientConf.style.mobileOnly)(
        a(ClientConf.style.linkHeaderFooter,
          produce(showMenu){ if(_) span("❌").render else span("☰").render},
          onclick :+= ((e:Event) => showMenu.set(!showMenu.get))
        )
      ),
      showIf(showMenu) {
        div(ClientConf.style.mobileMenu)(
          (links ++ uiMenu).map(div(_)),hr,
          user.map(frag(_,br)),otherMenu.map(span(_,br)),hr,
          Footer.copyright
        ).render
      }
    ).render
  }
}