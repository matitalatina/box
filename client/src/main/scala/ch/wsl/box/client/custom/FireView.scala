package ch.wsl.box.client.custom

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{FireState, ModelTableState}
import ch.wsl.box.client.views.{ModelTablePresenter, ModelTableViewPresenter}
import io.udash._
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 6/6/2017.
  */

object Fire {

  val tableState = ModelTableState("form", "fire")

  case object FireViewPresenter extends ViewPresenter[FireState.type] {
    override def create(): (View, Presenter[FireState.type]) = {
      val routes = Routes(tableState.kind,tableState.model)
      val (tableView, tablePresenter) = ModelTableViewPresenter(routes).create()
      val presenter = FirePresenter(tablePresenter)
      val view = FireView(tableView)
      (view,presenter)
    }
  }

  case class FirePresenter(table: Presenter[ModelTableState]) extends Presenter[FireState.type] {
    override def handleState(state: FireState.type): Unit = {
      table.handleState(tableState)
    }
  }

  case class FireView(table:View) extends View {
    override def renderChild(view: View): Unit = {}

    override def getTemplate: Modifier[Element] = table()
  }

}
