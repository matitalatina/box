package ch.wsl.box.client.views

/**
  * Created by andre on 4/3/2017.
  */

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.{ModelFormState, ModelTableState, ModelsState}
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.form.UdashForm
import io.udash.core.Presenter
import org.scalajs.dom.{Element, Event}


case class Models(list:Seq[String], model:Option[String], kind:Option[String], search:String, filteredList:Seq[String])

case object ModelsViewPresenter extends ViewPresenter[ModelsState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue


  override def create(): (View, Presenter[ModelsState]) = {
    val model = ModelProperty{
      Models(Seq(),None,None,"",Seq())
    }
    val presenter = new ModelsPresenter(model)
    val view = new ModelsView(model,presenter)
    (view,presenter)
  }
}

class ModelsPresenter(model:ModelProperty[Models]) extends Presenter[ModelsState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: ModelsState): Unit = {
    model.subProp(_.kind).set(Some(state.kind))
    REST.models(state.kind).map{ models =>
      model.subSeq(_.list).set(models)
      model.subSeq(_.filteredList).set(models)
    }
    if(state.model != "") {
      model.subProp(_.model).set(Some(state.model))
    } else {
      model.subProp(_.model).set(None)
    }
  }


  def updateModelsList() = {
    model.subProp(_.filteredList).set(model.subProp(_.list).get.filter(m => m.startsWith(model.get.search)))
  }

}

class ModelsView(model:ModelProperty[Models],presenter: ModelsPresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {

    import io.udash.wrappers.jquery._
    jQ(child).children().remove()
    if(view != null) {
      view.getTemplate.applyTo(child)
    }

  }

  private val child: Element = div().render

  override def getTemplate: scalatags.generic.Modifier[Element] = div(BootstrapStyles.row)(
    div(BootstrapStyles.Grid.colMd2)(
      UdashForm.textInput()("Search model")(model.subProp(_.search),onkeyup :+= ((ev: Event) => presenter.updateModelsList(), true)),
      produce(model.subProp(_.search)) { q =>
        ul(
          repeat(model.subSeq(_.filteredList)){m =>
            li(a(href := ModelTableState(model.subProp(_.kind).get.getOrElse(""),m.get).url)(m.get)).render
          }
        ).render
      }
    ),
    div(BootstrapStyles.Grid.colMd10)(
      produce(model)( m =>
        m.model match {
          case None => div(
            h1("Models"),
            p("select your model")
          ).render
          case Some(model) => div(
            a(href := ModelFormState(m.kind.getOrElse(""),model,None).url)("New " + model),
            a(href := ModelTableState(m.kind.getOrElse(""),model).url)("Table " + model)
          ).render
        }
      ),
      child
    )
  )
}
