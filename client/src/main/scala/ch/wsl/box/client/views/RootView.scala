package ch.wsl.box.client.views

import ch.wsl.box.client.services.Box
import io.udash._
import ch.wsl.box.client.{IndexState, ModelsState, RootState}
import org.scalajs.dom.Element

import scalatags.JsDom.tags2.main
import ch.wsl.box.client.views.components._
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.Presenter

import scalacss.ScalatagsCss._

case class RootViewModel()

case object RootViewPresenter extends ViewPresenter[RootState.type]{

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[RootState.type]) = {
    val model = ModelProperty{
      RootViewModel()
    }
    (new RootView(model),new RootPresenter(model))
  }
}

class RootPresenter(model:ModelProperty[RootViewModel]) extends Presenter[RootState.type] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: RootState.type): Unit = {
  }
}

class RootView(model:ModelProperty[RootViewModel]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  private val child: Element = div().render

  private def content = div(BootstrapStyles.containerFluid)(
    Header.navbar("box client",Seq(
      MenuLink("Home",IndexState),
      MenuLink("Models",ModelsState(""))
    )),
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