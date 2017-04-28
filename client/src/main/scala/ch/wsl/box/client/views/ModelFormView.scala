package ch.wsl.box.client.views

import ch.wsl.box.client.ModelFormState
import ch.wsl.box.client.services.Box
import ch.wsl.box.client.views.components.JSONSchemaRenderer
import ch.wsl.box.client.views.components.JSONSchemaRenderer.FormDefinition
import ch.wsl.box.model.shared.{JSONField, JSONSchema, JSONSchemaL2}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.UdashBootstrap
import io.udash.bootstrap.label.UdashLabel
import io.udash.core.Presenter
import org.scalajs.dom.{Element, Event}

import scala.util.Try


/**
  * Created by andre on 4/24/2017.
  */


case class ModelFormModel(name:String, form:Option[FormDefinition], results:Seq[String], error:String)

case object ModelFormViewPresenter extends ViewPresenter[ModelFormState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelFormState]) = {
    val model = ModelProperty{
      ModelFormModel("",None,Seq(),"")
    }
    val presenter = ModelFormPresenter(model)
    (ModelFormView(model,presenter),presenter)
  }
}

case class ModelFormPresenter(model:ModelProperty[ModelFormModel]) extends Presenter[ModelFormState] {
  import scalajs.concurrent.JSExecutionContext.Implicits.queue
  override def handleState(state: ModelFormState): Unit = {
    model.subProp(_.name).set(state.model)

    {for{
      schema <- Box.schema(state.model)
      form <- Box.form(state.model)
    } yield {

      //initialise an array of n strings, where n is the number of fields
      val results:Seq[String] = schema.properties.toSeq.map(_ => "")

      //the order here is relevant, changing the value on schema will trigger the view update so it needs the result array correctly set
      model.subSeq(_.results).set(results)
      model.subProp(_.form).set(Some(FormDefinition(schema,form)))

    }}.recover{ case e => e.printStackTrace() }

  }

  import io.circe.syntax._
  def parse(id:String,schema:JSONSchemaL2,value:Option[String]):(String,Json) = try{

    val data = schema.`type` match {
      case "string" => value.asJson
      case "number" => value.map( v => v.toDouble).asJson
    }

    (id,data)
  } catch { case t: Throwable =>
    model.subProp(_.error).set(s"Error parsing $id field: " + t.getMessage)
    throw t
  }

  def save() = {
    val m = model.get
    m.form.foreach{ form =>
        val jsons = for {
          ((name, property), i) <- form.schema.properties.zipWithIndex
        } yield parse(name, property, m.results.lift(i))
        Box.insert(m.name, jsons.toMap.asJson)
    }
  }

}

case class ModelFormView(model:ModelProperty[ModelFormModel],presenter:ModelFormPresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}


  override def getTemplate: scalatags.generic.Modifier[Element] = {

    div(
      h1(bind(model.subProp(_.name))),
      produce(model.subProp(_.error)){ error =>
        div(
          if(error.length > 0) {
            UdashLabel.danger(UdashBootstrap.newId(), error).render
          }
        ).render
      },
      produce(model.subProp(_.form)){ form =>
        div(
          JSONSchemaRenderer(form,model.subSeq(_.results).elemProperties)
        ).render
      },
      produce(model.subSeq(_.results)) { results =>
        ul(
          for(x <- results) yield {
            li(x)
          }
        ).render
      },
      produce(model.subProp(_.form)){form => p(form.toString()).render },
      button(
        cls := "primary",
        onclick :+= ((ev: Event) => presenter.save(), true)
      )("Save")
    )
  }
}
