package ch.wsl.box.client.views

import ch.wsl.box.client.ModelTableState
import io.udash._
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 4/24/2017.
  */


case class ModelTableModel(name:String)

case object ModelTableViewPresenter extends ViewPresenter[ModelTableState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty{
      ModelTableModel("")
    }

    (ModelTableView(model),ModelTablePresenter(model))
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel]) extends Presenter[ModelTableState]{
  override def handleState(state: ModelTableState): Unit = {
    model.subProp(_.name).set(state.model)
  }
}

case class ModelTableView(model:ModelProperty[ModelTableModel]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = div(
    h1(bind(model.subProp(_.name)))
  )
}
