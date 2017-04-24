package ch.wsl.box.client.views

/**
  * Created by andre on 4/3/2017.
  */

import ch.wsl.box.client.{ModelTableState, ModelsState}
import ch.wsl.box.client.services.Box
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.Presenter
import org.scalajs.dom.Element


case class Models(list:Seq[String], noChilds:Boolean)

case object ModelsViewPresenter extends ViewPresenter[ModelsState.type] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue


  override def create(): (View, Presenter[ModelsState.type]) = {
    val model = ModelProperty{
      Models(Seq(),true)
    }
    val presenter = new ModelsPresenter(model)
    val view = new ModelsView(model)
    (view,presenter)
  }
}

class ModelsPresenter(model:ModelProperty[Models]) extends Presenter[ModelsState.type] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: ModelsState.type): Unit = {
    Box.models().map{ models =>
      model.subSeq(_.list).set(models)
    }
  }
}

class ModelsView(model:ModelProperty[Models]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {

    import io.udash.wrappers.jquery._
    jQ(child).children().remove()
    if(view != null) {
      model.subProp(_.noChilds).set(false)
      view.getTemplate.applyTo(child)
    } else {
      model.subProp(_.noChilds).set(true)
    }

  }

  private val child: Element = div().render

  override def getTemplate: scalatags.generic.Modifier[Element] = div(BootstrapStyles.row)(
    div(BootstrapStyles.Grid.colMd2)(
      ul(
        repeat(model.subSeq(_.list))(m => li(a(href := ModelTableState(m.get).url)(m.get)).render)
      )
    ),
    div(BootstrapStyles.Grid.colMd10)(
      showIf(model.subProp(_.noChilds))(
        div(
          h1("Models"),
          p("select your model")
        ).render
      ),
      child
    )
  )
}
