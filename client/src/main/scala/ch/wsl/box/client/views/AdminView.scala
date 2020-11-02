package ch.wsl.box.client.views


import io.udash._
import ch.wsl.box.client._
import ch.wsl.box.client.services.{ClientConf, Navigate, REST}
import org.scalajs.dom.{Element, Event}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.model.shared._
import io.circe.Json
import scalacss.ScalatagsCss._
import ch.wsl.box.client.views.components.Debug
import io.udash.bootstrap.BootstrapStyles

import scala.concurrent.Future

case class AdminViewModel(entities:Seq[String])
object AdminViewModel extends HasModelPropertyCreator[AdminViewModel] {
  implicit val blank: Blank[AdminViewModel] =
    Blank.Simple(AdminViewModel(Seq()))
}

object AdminViewPresenter extends ViewFactory[AdminState.type]{

  val prop = ModelProperty.blank[AdminViewModel]

  override def create() = {
    val presenter = new AdminPresenter(prop)
    (new AdminView(prop,presenter),presenter)
  }
}

class AdminPresenter(viewModel:ModelProperty[AdminViewModel]) extends Presenter[AdminState.type] {

  import Context._

  override def handleState(state: AdminState.type): Unit = {
    for{
      entitites <- services.rest.entities(EntityKind.ENTITY.kind)
    } yield {
      viewModel.set(AdminViewModel(entitites))
    }
  }

  def generateStub(entity:String) = {
    services.rest.generateStub(entity)
  }
}

class AdminView(viewModel:ModelProperty[AdminViewModel], presenter:AdminPresenter) extends View {
  import scalatags.JsDom.all._
  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.JSONUtils._
  import io.udash.css.CssView._


  import org.scalajs.dom.File

  private val entityForStub = Property("")

  private val content = div(BootstrapStyles.Grid.row)(
    div(BootstrapCol.md(12),h2("Admin")),
    div(BootstrapCol.md(3),h3("Forms"),
      a("Interface builder", Navigate.click(EntityTableState(EntityKind.BOX.kind,"Interface builder"))),
      div(BootstrapStyles.Card.card,
        label("Generate STUB for"),br,
        Select( entityForStub, viewModel.subSeq(_.entities))(Select.defaultLabel).render,
        br,
        button(ClientConf.style.boxButtonImportant,"Generate", onclick :+= ((e:Event) => presenter.generateStub(entityForStub.get)))
      )
    ),
    div(BootstrapCol.md(3),h3("Functions"),
      a("Function builder", Navigate.click(EntityTableState(EntityKind.BOX.kind,"Function builder")))
    ),
    div(BootstrapCol.md(3),h3("Exports")),
    div(BootstrapCol.md(3),h3("Users")),
  )


  override def getTemplate: Modifier = content

}
