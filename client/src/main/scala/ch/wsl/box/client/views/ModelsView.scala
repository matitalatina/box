package ch.wsl.box.client.views

/**
  * Created by andre on 4/3/2017.
  */

import ch.wsl.box.client.{ModelFormState, ModelTableState, ModelsState}
import ch.wsl.box.client.services.Box
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.core.Presenter
import org.scalajs.dom.Element


case class Models(list:Seq[String], model:Option[String])

case object ModelsViewPresenter extends ViewPresenter[ModelsState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue


  override def create(): (View, Presenter[ModelsState]) = {
    val model = ModelProperty{
      Models(Seq(),None)
    }
    val presenter = new ModelsPresenter(model)
    val view = new ModelsView(model)
    (view,presenter)
  }
}

class ModelsPresenter(model:ModelProperty[Models]) extends Presenter[ModelsState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: ModelsState): Unit = {
    Box.models().map{ models =>
      model.subSeq(_.list).set(models)
    }
    if(state.model != "") {
      model.subProp(_.model).set(Some(state.model))
    } else {
      model.subProp(_.model).set(None)
    }
  }
}

class ModelsView(model:ModelProperty[Models]) extends View {
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
      ul(
        repeat(model.subSeq(_.list))(m => li(a(href := ModelTableState(m.get).url)(m.get)).render)
      )
    ),
    div(BootstrapStyles.Grid.colMd10)(
      produce(model)( m =>
        m.model match {
          case None => div(
            h1("Models"),
            p("select your model")
          ).render
          case Some(model) => div(
            a(href := ModelFormState(model,None).url)("New " + model),
            a(href := ModelTableState(model).url)("Table " + model)
          ).render
        }
      ),
      child
    )
  )
}
