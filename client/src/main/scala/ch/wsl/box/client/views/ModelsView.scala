package ch.wsl.box.client.views

/**
  * Created by andre on 4/3/2017.
  */

import ch.wsl.box.client.ModelsState
import ch.wsl.box.client.services.RestClient
import io.udash._
import io.udash.core.Presenter
import org.scalajs.dom.Element


case class Models(list:String)

case object ModelsViewPresenter extends ViewPresenter[ModelsState.type] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelsState.type]) = {
    val model = ModelProperty{
      Models("")
    }
    val presenter = new ModelsPresenter(model)
    val view = new ModelsView(model)
    (view,presenter)
  }
}

class ModelsPresenter(model:ModelProperty[Models]) extends Presenter[ModelsState.type] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: ModelsState.type): Unit = {
    RestClient.server.models(RestClient.basicAuthToken("postgres","")).map{ models =>
      model.subProp(_.list).set(models.mkString(","))
    }
  }
}

class ModelsView(model:ModelProperty[Models]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = ul(
    li(bind(model.subProp(_.list)))
  )
}
