package ch.wsl.box.client.views

import ch.wsl.box.client.{MasterChildState, ModelTableState}
import io.udash.ViewPresenter
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.{Presenter, View}
import io.udash.properties.model.ModelProperty
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 5/11/2017.
  */


object MasterChildViewPresenter extends ViewPresenter[MasterChildState]{

  override def create(): (View, Presenter[MasterChildState]) = {

    val (masterView,masterPresenter) = ModelTableViewPresenter.create()
    val (childView,childPresenter) = ModelTableViewPresenter.create()

    (MasterChildView(masterView,childView),MasterChildPresenter(masterPresenter,childPresenter))
  }
}

case class MasterChildPresenter(masterPresenter:Presenter[ModelTableState],childPresenter:Presenter[ModelTableState]) extends Presenter[MasterChildState] {
  override def handleState(state: MasterChildState): Unit = {
    masterPresenter.handleState(ModelTableState(state.parentModel))
    childPresenter.handleState(ModelTableState(state.childModel))
  }
}

case class MasterChildView(master:View,child:View) extends View {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = div(BootstrapStyles.row,
    h1("Master-Child"),
    div(BootstrapStyles.Grid.colXs12,master()),
    div(BootstrapStyles.Grid.colXs12,child())
  )
}


