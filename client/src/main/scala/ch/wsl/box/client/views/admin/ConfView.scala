package ch.wsl.box.client.views.admin

import ch.wsl.box.client._
import ch.wsl.box.client.services.{ClientConf, Navigate}
import ch.wsl.box.client.styles.BootstrapCol
import ch.wsl.box.client.views.components.widget.WidgetUtils
import ch.wsl.box.model.shared._
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom.Event
import scalacss.ScalatagsCss._
import io.circe.generic.auto._

case class ConfEntry(key:String,value:String)

case class ConfViewModel(entries:Seq[ConfEntry])
object ConfViewModel extends HasModelPropertyCreator[ConfViewModel] {
  implicit val blank: Blank[ConfViewModel] =
    Blank.Simple(ConfViewModel(Seq()))
}

object ConfViewPresenter extends ViewFactory[ConfState.type]{

  val prop = ModelProperty.blank[ConfViewModel]

  override def create() = {
    val presenter = new ConfPresenter(prop)
    (new ConfView(prop,presenter),presenter)
  }
}

class ConfPresenter(viewModel:ModelProperty[ConfViewModel]) extends Presenter[ConfState.type] {

  import Context._

  override def handleState(state: ConfState.type): Unit = {
    services.rest.list(EntityKind.ENTITY.kind,services.clientSession.lang(),"conf",10000).map{ confs =>
      val entries = confs.flatMap(js => js.as[ConfEntry].toOption)
      viewModel.subProp(_.entries).set(entries)
    }
  }

}

class ConfView(viewModel:ModelProperty[ConfViewModel], presenter:ConfPresenter) extends View {
  import io.udash.css.CssView._
  import scalatags.JsDom.all._

  private val entityForStub = Property("")

  private val content = div(BootstrapStyles.Grid.row)(
    div(BootstrapCol.md(12),h2("Conf")),
    div(BootstrapCol.md(3),h3("General"),
      div(BootstrapCol.md(12),ClientConf.style.noPadding,ClientConf.style.smallBottomMargin,
        label("host"),
        //TextInput(viewModel.transform()),
        div(BootstrapStyles.Visibility.clearfix)
      )
    ),
    div(BootstrapCol.md(3),h3("News"),
      ul(ClientConf.style.spacedList,
        li(
          a("News editor", Navigate.click(EntityTableState(EntityKind.BOX.kind,"news")))
        )
      )
    ),
    div(BootstrapCol.md(3),h3("Conf"),
      ul(ClientConf.style.spacedList,
        li(
          a("Import/Export Definitions", Navigate.click(AdminBoxDefinitionState))
        ),
        li(
          a("Labels", Navigate.click(FormPageState(EntityKind.BOX.kind,"labels","true",false)))
        )
      )
    )
  )


  override def getTemplate: Modifier = content

}
