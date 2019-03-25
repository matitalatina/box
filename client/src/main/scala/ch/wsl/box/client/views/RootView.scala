package ch.wsl.box.client.views

import ch.wsl.box.client.services.{Notification, REST}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.{ClientConf, Labels, Session, UI}
import io.udash._
import ch.wsl.box.client._
import org.scalajs.dom.Element
import scalatags.JsDom.tags2.main
import ch.wsl.box.client.views.components._
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.Presenter
import scalacss.ScalatagsCss._
import ch.wsl.box.client.Context._

case object RootViewPresenter extends ViewPresenter[RootState.type]{


  override def create(): (View, Presenter[RootState.type]) = {

    (new RootView(),new RootPresenter())
  }
}

class RootPresenter() extends Presenter[RootState.type] {


  override def handleState(state: RootState.type): Unit = {
  }
}

class RootView() extends ContainerView {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._

  private val child: Element = div().render


  private val menu = if(Session.isLogged()) {
    Seq(MenuLink(Labels.header.home,IndexState)) ++
      {if(UI.enableAllTables) {
        Seq(
          MenuLink("Admin",AdminState),
          MenuLink(Labels.header.entities,EntitiesState("entity","")),
          MenuLink("Tables",EntitiesState("table","")),
          MenuLink("Views",EntitiesState("view","")),
          MenuLink(Labels.header.forms,EntitiesState("form","")),
          MenuLink("Exports",DataListState(DataKind.EXPORT,"")),
          MenuLink("Functions",DataListState(DataKind.FUNCTION,""))
        )
      } else Seq()}
//      Seq(MenuLink("BoxTables", EntitiesState("table","")))
  } else Seq()


  private def content = div(BootstrapStyles.containerFluid)(
    Header.navbar(UI.title,menu),
    div(ClientConf.style.notificationArea,
      repeat(Notification.list){ notice =>
        div(ClientConf.style.notification,bind(notice)).render
      }
    ),
    main(ClientConf.style.fullHeight)(
      div()(
        child
      )
    ),
    Footer.template(UI.logo)
  )

  override def getTemplate: Modifier = content

  override def renderChild(view: Option[View]): Unit = {
    import io.udash.wrappers.jquery._
    jQ(child).children().remove()
    view.foreach(_.getTemplate.applyTo(child))
  }
}