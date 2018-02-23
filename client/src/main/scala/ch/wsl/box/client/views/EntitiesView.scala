package ch.wsl.box.client.views

/**
  * Created by andre on 4/3/2017.
  */

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.BootstrapCol
import ch.wsl.box.client.utils.{Labels, Session, UI}
import ch.wsl.box.client.{EntitiesState, EntityFormState, EntityTableState}
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.form.UdashForm
import io.udash.core.Presenter
import org.scalajs.dom.{Element, Event}
import ch.wsl.box.client.Context._

case class Entities(list:Seq[String], currentEntity:Option[String], kind:Option[String], search:String, filteredList:Seq[String])

case class EntitiesViewPresenter(kind:String, modelName:String, sidebarWidth:Int) extends ViewPresenter[EntitiesState] {



  override def create(): (View, Presenter[EntitiesState]) = {
    val model = ModelProperty{
      Entities(Seq(),None,None,"",Seq())
    }
    val routes = Routes(kind,modelName)
    val presenter = new EntitiesPresenter(model)
    val view = new EntitiesView(model,presenter,sidebarWidth,routes)
    (view,presenter)
  }
}

class EntitiesPresenter(model:ModelProperty[Entities]) extends Presenter[EntitiesState] {



  override def handleState(state: EntitiesState): Unit = {
    model.subProp(_.kind).set(Some(state.kind))
    REST.entities(state.kind).map{ models =>
      model.subSeq(_.list).set(models)
      model.subSeq(_.filteredList).set(models)
    }
    Session.resetQuery()
    if(state.currentEntity != "") {
      model.subProp(_.currentEntity).set(Some(state.currentEntity))
    } else {
      model.subProp(_.currentEntity).set(None)
    }
  }


  def updateEntitiesList() = {
    model.subProp(_.filteredList).set(model.subProp(_.list).get.filter(m => m.startsWith(model.get.search)))
  }

}

class EntitiesView(model:ModelProperty[Entities], presenter: EntitiesPresenter, sidebarWidth:Int, routes:Routes) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import ch.wsl.box.model.shared.EntityKind._

  val sidebarGrid = BootstrapCol.md(sidebarWidth)
  def contentGrid = if(UI.showEntitiesSidebar) BootstrapCol.md(12-sidebarWidth) else BootstrapCol.md(12)

  override def renderChild(view: View): Unit = {

    import io.udash.wrappers.jquery._
    jQ(content).children().remove()
    if(view != null) {
      view.getTemplate.applyTo(content)
    }

  }

  private val content: Element = div().render

  private def sidebar: Element = if(UI.showEntitiesSidebar) {
    div(sidebarGrid)(
      UdashForm.textInput()(Labels.entities.search)(model.subProp(_.search),onkeyup :+= ((ev: Event) => presenter.updateEntitiesList(), true)),
      produce(model.subProp(_.search)) { q =>
        ul(
          repeat(model.subSeq(_.filteredList)){m =>
            li(a(href := routes.entity(m.get).url)(m.get)).render
          }
        ).render
      }
    ).render
  } else div().render

  override def getTemplate: scalatags.generic.Modifier[Element] = div(BootstrapStyles.row)(
    sidebar,
    div(contentGrid)(
      produce(model)( m =>
        m.currentEntity match {
          case None => div(
            h1(Labels.entities.title),
            p(Labels.entities.select)
          ).render
          case Some(model) => div().render
        }
      ),
      content
    )
  )
}
