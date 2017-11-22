package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.utils.{IDSequence, Labels, Navigation, Session}
import ch.wsl.box.client.views.components.widget.Widget
import ch.wsl.box.client.views.components.{Debug, JSONSchemaRenderer}
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bootstrap.{BootstrapStyles, UdashBootstrap}
import io.udash.bootstrap.label.UdashLabel
import io.udash.core.Presenter
import org.scalajs.dom.{Element, Event}

import scala.concurrent.Future
import scalatags.JsDom


/**
  * Created by andre on 4/24/2017.
  */

case class ModelFormModel(name:String, kind:String, id:Option[String], form:Option[JSONMetadata], results:Seq[(String,Json)], error:String, subforms:Seq[JSONMetadata], navigation: Navigation,loading:Boolean)

object ModelFormModel{
  def empty = ModelFormModel("","",None,None,Seq(),"",Seq(),Navigation(false,false,0,0),true)
}

object ModelFormViewPresenter extends ViewPresenter[ModelFormState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelFormState]) = {
    val model = ModelProperty{ModelFormModel.empty}
    val presenter = ModelFormPresenter(model)
    (ModelFormView(model,presenter),presenter)
  }
}

case class ModelFormPresenter(model:ModelProperty[ModelFormModel]) extends Presenter[ModelFormState] {

  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JsonUtils._

  override def handleState(state: ModelFormState): Unit = {


    val reloadMetadata = {
      val currentModel = model.get
      !(currentModel.kind == state.kind &&
        currentModel.name == state.model &&
        currentModel.form.isDefined)
    }


    model.subProp(_.name).set(state.model)
    model.subProp(_.id).set(state.id)


    val ids = state.id.map(JSONKeys.fromString)

    {for{
      currentData <- state.id match {
        case Some(id) => REST.get(state.kind,Session.lang(),state.model,ids.get)
        case None => Future.successful(Json.Null)
      }
      form <- if(reloadMetadata) REST.form(state.kind, Session.lang(), state.model) else Future.successful(model.get.form.get)
      subforms <- if(state.kind == "form" && reloadMetadata) REST.subforms(state.model,Session.lang()) else Future.successful(Seq())
    } yield {


      //initialise an array of n strings, where n is the number of fields
      val results:Seq[(String,Json)] = Enhancer.extract(currentData,form)

      model.set(ModelFormModel(
        name = state.model,
        kind = state.kind,
        id = state.id,
        form = Some(form),
        results,
        "",
        subforms,
        Navigation(false,false,1,1),
        false
      ))

      setNavigation()

    }}.recover{ case e => e.printStackTrace() }

  }

  import io.circe.syntax._

  def save() = {
    val m = model.get
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
        val newState = Routes(m.kind,m.name).table()
        io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)

      }.recover{ case e =>
        e.getStackTrace.foreach(x => println(s"file ${x.getFileName}.${x.getMethodName}:${x.getLineNumber}"))
        e.printStackTrace()
      }
    }
  }


  def setNavigation() = {
    IDSequence(model.get.id).navigation().map{ nav =>
      model.subProp(_.navigation).set(nav)
    }
  }

  private var widget:Widget = new Widget {
    import scalatags.JsDom.all._
    override def render(): JsDom.all.Modifier = div()
  }

  def loadWidgets(f:JSONMetadata) = {
    widget = JSONSchemaRenderer(f, model.subSeq(_.results), model.subProp(_.subforms).get).widget()
    widget
  }

  def next() = IDSequence(model.get.id).next(model.get.kind,model.get.name).map(_.map(goTo))
  def prev() = IDSequence(model.get.id).prev(model.get.kind,model.get.name).map(_.map(goTo))

  def goTo(id:String) = {
    model.subProp(_.loading).set(true)
    val m = model.get
    val newState = Routes(m.kind,m.name).edit(id)
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }


}

case class ModelFormView(model:ModelProperty[ModelFormModel],presenter:ModelFormPresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe.generic.auto._

  override def renderChild(view: View): Unit = {}


  override def getTemplate: scalatags.generic.Modifier[Element] = {

    div(
      h1(
        bind(model.subProp(_.name)),
        showIf(model.subProp(_.loading)) {
          small(" - " + Labels.navigation.loading).render
        },
        produce(model.subProp(_.id)){ id =>
          val subTitle = id.map(" - " + _).getOrElse("")
          small(subTitle).render
        }

      ),
      div(
        showIf(model.subProp(_.navigation.hasPrevious)) { a(onclick :+= ((ev: Event) => presenter.prev(), true), Labels.navigation.previous).render },
        span(
          bind(model.subProp(_.navigation.current)),
          " of ",
          bind(model.subProp(_.navigation.count))
        ),
        showIf(model.subProp(_.navigation.hasNext)) { a(onclick :+= ((ev: Event) => presenter.next(), true),Labels.navigation.next).render }
      ),
      produce(model.subProp(_.error)){ error =>
        div(
          if(error.length > 0) {
            UdashLabel.danger(UdashBootstrap.newId(), error).render
          }
        ).render
      },
      br,
      hr,
      produce(model.subProp(_.form)){ form =>
        div(
          form match {
            case None => p("Loading form")
            case Some(f) => presenter.loadWidgets(f).render()
          }
        ).render
      },
      button(
        cls := "primary",
        onclick :+= ((ev: Event) => presenter.save(), true)
      )(Labels.form.save),br,br,
      Debug(model.subProp(_.form)),
      Debug(model.subProp(_.results))
    )
  }
}
