package ch.wsl.box.client.views

import ch.wsl.box.client.ModelFormState
import io.udash._
import io.udash.core.Presenter
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 4/24/2017.
  */

case class ModelFormModel(name:String)

case object ModelFormViewPresenter extends ViewPresenter[ModelFormState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelFormState]) = {
    val model = ModelProperty{
      ModelFormModel("")
    }
    (ModelFormView(model),ModelFormPresenter(model))
  }
}

case class ModelFormPresenter(model:ModelProperty[ModelFormModel]) extends Presenter[ModelFormState] {
  override def handleState(state: ModelFormState): Unit = {
    model.subProp(_.name).set(state.model)
  }
}

case class ModelFormView(model:ModelProperty[ModelFormModel]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = div(
    h1(bind(model.subProp(_.name))),
    p("form")
  )
}
