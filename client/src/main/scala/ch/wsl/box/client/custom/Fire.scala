package ch.wsl.box.client.custom

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{FireFormState, FireState, ModelTableState, RoutingState}
import ch.wsl.box.client.views.{ModelTablePresenter, ModelTableViewPresenter}
import io.udash._
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 6/6/2017.
  */

object Fire {

  val routes = new Routes {
    override def add(): RoutingState = FireFormState(None)
    override def edit(id: String): RoutingState = FireFormState(Some(id))
    override def table(): RoutingState = FireState
  }

  val tableState = ModelTableState("form", "fire")

  case object FireViewPresenter extends ViewPresenter[FireState.type] {
    override def create(): (View, Presenter[FireState.type]) = {
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
    import ch.wsl.box.client.Context._
    import scalatags.JsDom.all._

    override def renderChild(view: View): Unit = {}

    override def getTemplate = div(
      h1("TEST"),
      table()
    )
  }

}
