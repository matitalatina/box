package ch.wsl.box.client.views

import ch.wsl.box.client.services.{Notification, REST}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.{Labels, Session, UI}
import io.udash._
import ch.wsl.box.client.{EntitiesState, IndexState, RootState}
import org.scalajs.dom.Element

import scalatags.JsDom.tags2.main
import ch.wsl.box.client.views.components._
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.Presenter

import scalacss.ScalatagsCss._


case object RootViewPresenter extends ViewPresenter[RootState.type]{

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[RootState.type]) = {

    (new RootView(),new RootPresenter())
  }
}

class RootPresenter() extends Presenter[RootState.type] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: RootState.type): Unit = {
  }
}

class RootView() extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  private val child: Element = div().render


  private val menu = if(Session.isLogged()) {
    Seq(MenuLink(Labels.header.home,IndexState)) ++
      {if(UI.enableAllTables) {
        Seq(
          MenuLink(Labels.header.entities,EntitiesState("entity","")),
          MenuLink("Tables",EntitiesState("table","")),
          MenuLink("Views",EntitiesState("view","")),
          MenuLink(Labels.header.forms,EntitiesState("form",""))
        )
      } else Seq()}
  } else Seq()


  private def content = div(BootstrapStyles.containerFluid)(
    Header.navbar(UI.logo,UI.title,menu),
    div(GlobalStyles.notificationArea,
      repeat(Notification.list){ notice =>
        div(GlobalStyles.notification,bind(notice)).render
      }
    ),
    main()(
      div()(
        child
      )
    )
,Footer.getTemplate
  )

  override def getTemplate: Modifier = content

  override def renderChild(view: View): Unit = {
    import io.udash.wrappers.jquery._
    jQ(child).children().remove()
    view.getTemplate.applyTo(child)
  }
}