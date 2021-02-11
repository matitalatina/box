package ch.wsl.box.client.views.admin

import ch.wsl.box.client._
import ch.wsl.box.client.services.{ClientConf, Navigate}
import ch.wsl.box.client.styles.BootstrapCol
import ch.wsl.box.model.shared._
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom.Event
import scalacss.ScalatagsCss._

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
  import io.udash.css.CssView._
  import scalatags.JsDom.all._

  private val entityForStub = Property("")

  private val content = div(BootstrapStyles.Grid.row)(
    div(BootstrapCol.md(12),h2("Admin")),
    div(BootstrapCol.md(3),h3("Forms"),
      a("Interface builder", Navigate.click(EntityTableState(EntityKind.BOX.kind,"form"))),
      div(BootstrapStyles.Card.card,
        label("Generate STUB for"),br,
        Select( entityForStub, viewModel.subSeq(_.entities))(Select.defaultLabel).render,
        br,
        button(ClientConf.style.boxButtonImportant,"Generate", onclick :+= ((e:Event) => presenter.generateStub(entityForStub.get)))
      )
    ),
    div(BootstrapCol.md(3),h3("Functions"),
      a("Function builder", Navigate.click(EntityTableState(EntityKind.BOX.kind,"function")))
    ),
    div(BootstrapCol.md(3),h3("News"),
      a("News editor", Navigate.click(EntityTableState(EntityKind.BOX.kind,"news")))
    ),
    div(BootstrapCol.md(3),h3("Import/Export"),
      a("Definitions", Navigate.click(AdminBoxDefinitionState))
    )
  )


  override def getTemplate: Modifier = content

}
