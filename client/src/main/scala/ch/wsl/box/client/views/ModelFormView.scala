package ch.wsl.box.client.views

import ch.wsl.box.client.ModelFormState
import ch.wsl.box.client.services.Box
import ch.wsl.box.client.views.components.JSONSchemaRenderer
import io.udash._
import io.udash.core.Presenter
import org.scalajs.dom.Element

import scalatags.generic.Modifier

/**
  * Created by andre on 4/24/2017.
  */

case class JSONSchema(
                       `type`:String,
                       title:Option[String] = None,
                       required: Option[Seq[String]] = None,
                       readonly: Option[Boolean] = None,
                       enum: Option[Seq[String]] = None,
                       order: Option[Int] = None
                     )
case class ModelFormModel(name:String, schema:JSONSchema)

case object ModelFormViewPresenter extends ViewPresenter[ModelFormState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelFormState]) = {
    val model = ModelProperty{
      ModelFormModel("",JSONSchema(""))
    }
    (ModelFormView(model),ModelFormPresenter(model))
  }
}

case class ModelFormPresenter(model:ModelProperty[ModelFormModel]) extends Presenter[ModelFormState] {
  import scalajs.concurrent.JSExecutionContext.Implicits.queue
  override def handleState(state: ModelFormState): Unit = {
    model.subProp(_.name).set(state.model)
    println("model form handle state")
    Box.schema(state.model).map{ schema =>
      println("schema:" + schema)
      //model.subModel(_.schema).set(schema)
    }.recover{ case e => e.printStackTrace() }
    Box.list(state.model).map{ elements =>
      println("elements")
      //      model.subModel(_.schema).set(schema)
    }.recover{ case e => e.printStackTrace() }
  }
}

case class ModelFormView(model:ModelProperty[ModelFormModel]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = div(
    h1(bind(model.subProp(_.name))),
    produce(model.subModel(_.schema)){ schema =>
//      JSONSchemaRenderer(schema).render
      p("test").render
    }
  )
}
