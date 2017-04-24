package ch.wsl.box.client.views

/**
  * Created by andre on 4/3/2017.
  */

import ch.wsl.box.client.ModelsState
import ch.wsl.box.client.services.Box
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.Presenter
import org.scalajs.dom.Element


case class Models(list:Seq[String])

case object ModelsViewPresenter extends ViewPresenter[ModelsState.type] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue


  override def create(): (View, Presenter[ModelsState.type]) = {
    val model = ModelProperty{
      Models(Seq())
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

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = div(BootstrapStyles.row)(
    div(BootstrapStyles.Grid.colXs2)(
      ul(
        repeat(model.subSeq(_.list))(m => li(m.get).render)
      )
    ),
    div(BootstrapStyles.Grid.colXs10)(
      h1("Models"),
      p("select your model")
    )
  )
}
