package ch.wsl.box.client.views


/**
  * Created by andre on 4/3/2017.
  */

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.{Navigate, REST}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.{Labels, Session, UI}
import ch.wsl.box.client.{ExportState, ExportsState}
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.form.UdashForm
import io.udash.core.Presenter
import org.scalajs.dom.{Element, Event}
import ch.wsl.box.client.Context._
import ch.wsl.box.model.shared.ExportDef
import scalatags.generic

case class Exports(list:Seq[ExportDef], currentEntity:Option[ExportDef], search:String, filteredList:Seq[ExportDef])
object Exports extends HasModelPropertyCreator[Exports] {
  implicit val blank: Blank[Exports] =
    Blank.Simple(Exports(Seq(),None,"",Seq()))
}

case class ExportsViewPresenter(modelName:String) extends ViewFactory[ExportsState] {



  override def create(): (View, Presenter[ExportsState]) = {
    val model = ModelProperty.blank[Exports]

    val presenter = new ExportsPresenter(model)
    val view = new ExportsView(model,presenter)
    (view,presenter)
  }
}

class ExportsPresenter(model:ModelProperty[Exports]) extends Presenter[ExportsState] {



  override def handleState(state: ExportsState ): Unit = {
//    println(state.currentExport)
    REST.exports(Session.lang()).map{ exports =>
      model.subSeq(_.list).set(exports)
      model.subSeq(_.filteredList).set(exports)
      val current = exports.find(_.function == state.currentExport)

      model.subProp(_.currentEntity).set(current)
    }
  }


  def updateExportsList() = {
    model.subProp(_.filteredList).set(model.subProp(_.list).get.filter(m => m.label.contains(model.get.search)))
  }

}

class ExportsView(model:ModelProperty[Exports], presenter: ExportsPresenter) extends ContainerView {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import io.udash.css.CssView._


  val sidebarGrid = BootstrapCol.md(2)
  def contentGrid =  BootstrapCol.md(10)

  override def renderChild(view: Option[View]): Unit = {

    import io.udash.wrappers.jquery._
    jQ(content).children().remove()
    if(view.isDefined) {
      view.get.getTemplate.applyTo(content)
    }

  }


  private val content: Element = div().render

  private def sidebar: Element = div(sidebarGrid)(
      UdashForm.textInput()(Labels.exports.search)(model.subProp(_.search),onkeyup :+= ((ev: Event) => presenter.updateExportsList(), true)),
      produce(model.subProp(_.search)) { q =>
        ul(GlobalStyles.noBullet)(
          repeat(model.subSeq(_.filteredList)){m =>
            li(produce(m) { export =>
              a(Navigate.click(ExportState(export.function)), m.get.label).render
            }).render
          }
        ).render
      }
    ).render

  override def getTemplate: scalatags.generic.Modifier[Element] = div(BootstrapStyles.row)(
    sidebar,
    div(contentGrid)(
      div(h1(Labels.exports.title)).render,
      produce(model)( m =>
        m.currentEntity match {
          case None => div(
            p(Labels.exports.select)
          ).render
          case Some(model) => div().render
        }
      ),
      content
    )
  )
}
