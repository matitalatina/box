package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{EntityTableState, MasterChildState}
import ch.wsl.box.model.shared.{Filter, JSONField, JSONID}
import io.udash.ViewPresenter
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.{Presenter, View, ViewFactory}
import io.udash.properties.model.ModelProperty
import org.scalajs.dom.Element
import scribe.Logging
import scalatags.generic.Modifier

/**
  * Created by andre on 5/11/2017.
  */


case class MasterChildViewPresenter(master:String,child:String) extends ViewFactory[MasterChildState] with Logging {

  override def create(): (View, Presenter[MasterChildState]) = {

    val (childView,childPresenter) = EntityTableViewPresenter(Routes("entity",child)).create()

    def onChangeMaster(rows:Seq[(JSONField,String)]):Unit = {
      logger.info("change master")
      val ids = rows.filter(_._1.lookup.exists(_.lookupEntity == child))
      val childTable = childPresenter.asInstanceOf[EntityTablePresenter]
      if(ids.length > 0)
        childTable.filterById(JSONID.fromMap(ids.map(x => x._1.name -> x._2).toMap))

      val childForeignMetadata = childTable.model.get.fieldQueries.find(_.field.lookup.exists(_.lookupEntity == master))
      logger.debug(childForeignMetadata.toString)
      for{
        metadata <- childForeignMetadata
        value <- rows.find(_._1.name == metadata.field.lookup.get.map.valueProperty)
      } yield {
        childTable.filter(metadata.copy(filterValue = value._2,filterOperator = Filter.EQUALS),value._2)
      }


    }

    val (masterView,masterPresenter) = EntityTableViewPresenter(Routes("entity",master),onChangeMaster).create()


    (MasterChildView(masterView,childView),MasterChildPresenter(masterPresenter,childPresenter))
  }
}

case class MasterChildPresenter(masterPresenter:Presenter[EntityTableState], childPresenter:Presenter[EntityTableState]) extends Presenter[MasterChildState] {
  override def handleState(state: MasterChildState): Unit = {
    masterPresenter.handleState(EntityTableState(state.kind,state.masterEntity))
    childPresenter.handleState(EntityTableState(state.kind,state.childEntity))
  }
}

case class MasterChildView(master:View,child:View) extends View {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._


  override def getTemplate: scalatags.generic.Modifier[Element] = div(BootstrapStyles.row,
    h1("Master-Child"),
    div(BootstrapStyles.Grid.colXs12,master()),
    div(BootstrapStyles.Grid.colXs12,child())
  )
}


