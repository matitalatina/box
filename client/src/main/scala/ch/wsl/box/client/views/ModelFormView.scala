package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.utils.{IDSequence, Navigation, Session}
import ch.wsl.box.client.views.components.JSONSchemaRenderer
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bootstrap.{BootstrapStyles, UdashBootstrap}
import io.udash.bootstrap.label.UdashLabel
import io.udash.core.Presenter
import org.scalajs.dom.{Element, Event}

import scala.concurrent.Future


/**
  * Created by andre on 4/24/2017.
  */

case class ModelFormModel(name:String, kind:String, id:Option[String], form:Option[JSONForm], results:Seq[(String,Json)], error:String,subforms:Seq[JSONForm],navigation: Navigation)

object ModelFormModel{
  def empty = ModelFormModel("","",None,None,Seq(),"",Seq(),Navigation(false,false,0,0))
}

case class ModelFormViewPresenter(routes:Routes) extends ViewPresenter[ModelFormState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelFormState]) = {
    val model = ModelProperty{ModelFormModel.empty}
    val presenter = ModelFormPresenter(model,routes)
    (ModelFormView(model,presenter),presenter)
  }
}

case class ModelFormPresenter(model:ModelProperty[ModelFormModel],routes:Routes) extends Presenter[ModelFormState] {

  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JsonUtils._

  override def handleState(state: ModelFormState): Unit = {
    println(s"handle state: $state")
    //model.set(ModelFormModel.empty)
    model.subProp(_.name).set(state.model)
    model.subProp(_.id).set(state.id)


    val ids = state.id.map(JSONKeys.fromString)

    {for{
      current <- state.id match {
        case Some(id) => REST.get(state.kind,Session.lang(),state.model,ids.get)
        case None => Future.successful(Json.Null)
      }
      emptyFieldsForm <- REST.form(state.kind,Session.lang(),state.model)
      emptySubforms <- if(state.kind == "form") REST.subforms(state.model,Session.lang()) else Future.successful(Seq())
      models <- Enhancer.fetchModels(emptySubforms ++ Seq(emptyFieldsForm))
    } yield {

      val form = Enhancer.populateOptionsValuesInFields(models,emptyFieldsForm,Seq(current))
      val subforms = emptySubforms.map{ f =>
        Enhancer.populateOptionsValuesInFields(models,f,current.seq(form.fields.find(_.subform.exists(_.id == f.id)).get.key))
      }

      //initialise an array of n strings, where n is the number of fields
      val results:Seq[(String,Json)] = Enhancer.extract(current,form)

      model.set(ModelFormModel(
        name = state.model,
        kind = state.kind,
        id = state.id,
        form = Some(form),
        results,
        "",
        subforms,
        Navigation(false,false,1,1)
      ))

      setNavigation()

    }}.recover{ case e => e.printStackTrace() }

  }

  import io.circe.syntax._

  def save() = {
    val m = model.get
    println(m.results)
    m.form.foreach{ form =>
      val jsons = for {
        (field, i) <- form.fields.zipWithIndex
      } yield Enhancer.parse(field, m.results.lift(i).map(_._2),form.keys){ t =>
        model.subProp(_.error).set(s"Error parsing ${field.key} field: " + t.getMessage)
      }
      val saveAction = m.id match {
        case Some(id) => REST.update(m.kind,Session.lang(),m.name,JSONKeys.fromString(id),jsons.toMap.asJson)
        case None => REST.insert(m.kind,Session.lang(),m.name, jsons.toMap.asJson)
      }
      saveAction.map{_ =>
        val newState = routes.table()
        io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)

      }.recover{ case e =>
        e.getStackTrace.foreach(x => println(s"file ${x.getFileName}.${x.getMethodName}:${x.getLineNumber}"))
        e.printStackTrace()
      }
    }
  }


  def setNavigation() = {
    IDSequence(model.get.id).navigation().map{ nav =>
      println(s"nav: $nav")
      model.subProp(_.navigation).set(nav)
    }
  }



  def next() = IDSequence(model.get.id).next(model.get.kind,model.get.name).map(_.map(goTo))
  def prev() = IDSequence(model.get.id).prev(model.get.kind,model.get.name).map(_.map(goTo))

  def goTo(id:String) = {
    val newState = routes.edit(id)
    println(s"newstate: ${newState.url}")
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
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
      div(
        showIf(model.subProp(_.navigation.hasPrevious)) { a(onclick :+= ((ev: Event) => presenter.prev(), true), "Previous").render },
        span(
          bind(model.subProp(_.navigation.current)),
          " of ",
          bind(model.subProp(_.navigation.count))
        ),
        showIf(model.subProp(_.navigation.hasNext)) { a(onclick :+= ((ev: Event) => presenter.next(), true),"Next").render }
      ),
      produce(model.subProp(_.error)){ error =>
        div(
          if(error.length > 0) {
            UdashLabel.danger(UdashBootstrap.newId(), error).render
          }
        ).render
      },
      produce(model.subProp(_.form)){ form =>
        div(
          form match {
            case None => p("Loading form")
            case Some(f) => JSONSchemaRenderer(f,model.subSeq(_.results),model.subProp(_.subforms).get)
          }
        ).render
      },
      button(
        cls := "primary",
        onclick :+= ((ev: Event) => presenter.save(), true)
      )("Save")
    )
  }
}
