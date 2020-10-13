package ch.wsl.box.client.views

import ch.wsl.box.client.services.{ClientConf, Notification, REST, UI}
import ch.wsl.box.client.styles.GlobalStyles
import io.udash._
import ch.wsl.box.client._
import org.scalajs.dom.Element
import scalatags.JsDom.tags2.main
import ch.wsl.box.client.views.components._
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.Presenter
import scalacss.ScalatagsCss._
import ch.wsl.box.client.Context._

case object RootViewPresenter extends ViewFactory[RootState.type]{


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





  private def content = div(BootstrapStyles.containerFluid)(
    Header.navbar(UI.title),
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
