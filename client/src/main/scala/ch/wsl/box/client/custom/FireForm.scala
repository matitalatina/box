package ch.wsl.box.client.custom

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.views.ModelFormViewPresenter
import ch.wsl.box.client.{FireFormState, ModelFormState}
import io.udash._
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 6/6/2017.
  */
object FireForm {
  def state(id:Option[String]) = ModelFormState("form","fire",id)

  case object FireViewPresenter extends ViewPresenter[FireFormState] {
    override def create(): (View, Presenter[FireFormState]) = {
      val (formView,formPresenter) = ModelFormViewPresenter(Fire.routes).create()
      val presenter = FirePresenter(formPresenter)
      val view = FireView(formView)
      (view,presenter)
    }
  }

  case class FirePresenter(form:Presenter[ModelFormState]) extends Presenter[FireFormState] {
    override def handleState(s: FireFormState): Unit = {
      form.handleState(state(s.id))
    }
  }

  case class FireView(form:View) extends View {
    override def renderChild(view: View): Unit = {}

    override def getTemplate: Modifier[Element] = form()
  }
}
