package ch.wsl.box.client.views

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.views.components.JSONSchemaRenderer
import ch.wsl.box.client.views.components.JSONSchemaRenderer.FormDefinition
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bootstrap.UdashBootstrap
import io.udash.bootstrap.label.UdashLabel
import io.udash.core.Presenter
import org.scalajs.dom.{Element, Event}

import scala.concurrent.Future
import scala.util.Try


/**
  * Created by andre on 4/24/2017.
  */


case class ModelFormModel(name:String, id:Option[String], form:Option[FormDefinition], results:Seq[String], error:String, keys:Seq[String])

case object ModelFormViewPresenter extends ViewPresenter[ModelFormState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelFormState]) = {
    val model = ModelProperty{
      ModelFormModel("",None,None,Seq(),"",Seq())
    }
    val presenter = ModelFormPresenter(model)
    (ModelFormView(model,presenter),presenter)
  }
}

case class ModelFormPresenter(model:ModelProperty[ModelFormModel]) extends Presenter[ModelFormState] {

  import ch.wsl.box.client.Context._

  override def handleState(state: ModelFormState): Unit = {
    model.subProp(_.name).set(state.model)
    model.subProp(_.id).set(state.id)
    println(state)


    {for{
      keys <- REST.keys(state.model)
      ids = state.id.map(JSONKeys.fromString)
      schema <- REST.schema(state.model)
      emptyFields <- REST.form(state.model)
      current <- state.id match {
        case Some(id) => REST.get(state.model,ids.get)
        case None => Future.successful(Json.Null)
      }
      fields <- Enhancer.populateOptionsValuesInFields(emptyFields)
    } yield {



      //initialise an array of n strings, where n is the number of fields
      val results:Seq[String] = Enhancer.extract(current,fields)

      //the order here is relevant, changing the value on schema will trigger the view update so it needs the result array correctly set
      model.subProp(_.keys).set(keys)
      model.subSeq(_.results).set(results)
      model.subProp(_.form).set(Some(FormDefinition(schema,fields)))

    }}.recover{ case e => e.printStackTrace() }

  }

  import io.circe.syntax._

  def save() = {
    val m = model.get
    println(m.results)
    m.form.foreach{ form =>
      val jsons = for {
        (field, i) <- form.fields.zipWithIndex
      } yield Enhancer.parse(field, m.results.lift(i)){ t =>
        model.subProp(_.error).set(s"Error parsing ${field.key} field: " + t.getMessage)
      }
      val saveAction = m.id match {
        case Some(id) => REST.update(m.name,JSONKeys.fromString(id),jsons.toMap.asJson)
        case None => REST.insert(m.name, jsons.toMap.asJson)
      }
      saveAction.map{_ =>

        val newState = ModelTableState(model.subProp(_.name).get)
        io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)

      }
    }
  }




}

case class ModelFormView(model:ModelProperty[ModelFormModel],presenter:ModelFormPresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}


  override def getTemplate: scalatags.generic.Modifier[Element] = {

    div(
      h1(bind(model.subProp(_.name)),produce(model.subProp(_.id)){ id =>
        val subTitle = id.map(" - " + _).getOrElse("")
        small(subTitle).render
      }),
      produce(model.subProp(_.error)){ error =>
        div(
          if(error.length > 0) {
            UdashLabel.danger(UdashBootstrap.newId(), error).render
          }
        ).render
      },
      produce(model.subProp(_.form)){ form =>
        div(
          JSONSchemaRenderer(form,model.subSeq(_.results).elemProperties,model.get.keys)
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
