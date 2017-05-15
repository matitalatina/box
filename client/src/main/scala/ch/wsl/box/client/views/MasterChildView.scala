package ch.wsl.box.client.views

import ch.wsl.box.client.{MasterChildState, ModelTableState}
import ch.wsl.box.model.shared.{Filter, JSONField, JSONKeys}
import io.udash.ViewPresenter
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.{Presenter, View}
import io.udash.properties.model.ModelProperty
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 5/11/2017.
  */


case class MasterChildViewPresenter(master:String,child:String) extends ViewPresenter[MasterChildState]{

  override def create(): (View, Presenter[MasterChildState]) = {

    val (childView,childPresenter) = ModelTableViewPresenter().create()

    def onChangeMaster(rows:Seq[(JSONField,String)]):Unit = {
      println("change master")
      val keys = rows.filter(_._1.options.exists(_.refModel == child))
      val childTable = childPresenter.asInstanceOf[ModelTablePresenter]
      if(keys.length > 0)
        childTable.filterByKey(JSONKeys.fromMap(keys.map(x => x._1.key -> x._2).toMap))

      val childForeignMetadata = childTable.model.get.metadata.find(_.field.options.exists(_.refModel == master))
      println(childForeignMetadata)
      for{
        metadata <- childForeignMetadata
        value <- rows.find(_._1.key == metadata.field.options.get.map.valueProperty)
      } yield {
        childTable.filter(metadata.copy(filter = value._2,filterType = Filter.EQUALS),value._2)
      }


    }

    val (masterView,masterPresenter) = ModelTableViewPresenter(onChangeMaster).create()


    (MasterChildView(masterView,childView),MasterChildPresenter(masterPresenter,childPresenter))
  }
}

case class MasterChildPresenter(masterPresenter:Presenter[ModelTableState],childPresenter:Presenter[ModelTableState]) extends Presenter[MasterChildState] {
  override def handleState(state: MasterChildState): Unit = {
    masterPresenter.handleState(ModelTableState(state.kind,state.parentModel))
    childPresenter.handleState(ModelTableState(state.kind,state.childModel))
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


