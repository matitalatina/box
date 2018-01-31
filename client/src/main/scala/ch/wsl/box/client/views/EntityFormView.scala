package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{EntityFormState, EntityTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.utils.{IdNav, Labels, Navigation, Session}
import ch.wsl.box.client.views.components.widget.Widget
import ch.wsl.box.client.views.components.{Debug, JSONMetadataRenderer}
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bootstrap.{BootstrapStyles, UdashBootstrap}
import io.udash.bootstrap.label.UdashLabel
import io.udash.core.Presenter
import io.udash.properties.single.Property
import org.scalajs.dom.{Element, Event}

import scala.concurrent.Future
import scalatags.JsDom


/**
  * Created by andre on 4/24/2017.
  */

case class EntityFormModel(name:String, kind:String, id:Option[String], metadata:Option[JSONMetadata], data:Json,
                           error:String, children:Seq[JSONMetadata], navigation: Navigation, loading:Boolean)

object EntityFormModel{
  def empty = EntityFormModel("","",None,None,Json.Null,"",Seq(), Navigation.empty0,true)
}

object EntityFormViewPresenter extends ViewPresenter[EntityFormState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[EntityFormState]) = {
    val model = ModelProperty{EntityFormModel.empty}
    val presenter = EntityFormPresenter(model)
    (EntityFormView(model,presenter),presenter)
  }
}

case class EntityFormPresenter(model:ModelProperty[EntityFormModel]) extends Presenter[EntityFormState] {

  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JsonUtils._

  override def handleState(state: EntityFormState): Unit = {


    val reloadMetadata = {
      val currentModel = model.get

      !(currentModel.kind == state.kind &&
        currentModel.name == state.entity &&
        currentModel.metadata.isDefined)
    }

    model.subProp(_.kind).set(state.kind)
    model.subProp(_.name).set(state.entity)
    model.subProp(_.id).set(state.id)


    val jsonId = state.id.map(JSONID.fromString)

    {for{
      currentData <- state.id match {
        case Some(id) => REST.get(state.kind, Session.lang(), state.entity,jsonId.get)
        case None => Future.successful(Json.Null)
      }
      metadata <- if(reloadMetadata) REST.metadata(state.kind, Session.lang(), state.entity) else Future.successful(model.get.metadata.get)
      children <- if(state.kind == "form" && reloadMetadata) REST.children(state.entity,Session.lang()) else Future.successful(Seq())
    } yield {


      //initialise an array of n strings, where n is the number of fields
      val results:Seq[(String,Json)] = Enhancer.extract(currentData,metadata)

      model.set(EntityFormModel(
        name = state.entity,
        kind = state.kind,
        id = state.id,
        metadata = Some(metadata),
        currentData,
        "",
        children,
        Navigation.empty1,
        false
      ))

      setNavigation()

    }}.recover{ case e => e.printStackTrace() }

  }

  import io.circe.syntax._
  import ch.wsl.box.client._

  def save(toState:(String, String) => RoutingState) = {

    val m = model.get
    m.metadata.foreach{ metadata =>
//      val jsons = for {
//        (field, i) <- form.fields.zipWithIndex
//      } yield Enhancer.parse(field, m.results.lift(i).map(_._2),form.keys){ t =>
//        model.subProp(_.error).set(s"Error parsing ${field.key} field: " + t.getMessage)
//      }

      val data:Json = m.data

      def saveAction() = m.id match {
        case Some(id) => REST.update(m.kind,Session.lang(),m.name,JSONID.fromString(id),data)
        case None => REST.insert(m.kind,Session.lang(),m.name, data)
      }

      {for{
        _ <- widget.beforeSave(data,metadata)
        resultSaved <- saveAction()
        _ <- widget.afterSave(resultSaved,metadata)
      } yield {
//        val newState =  Routes(m.kind,m.name).table()
        val newState =  toState(m.kind,m.name)
        io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)

      }}.recover{ case e =>
        e.getStackTrace.foreach(x => println(s"file ${x.getFileName}.${x.getMethodName}:${x.getLineNumber}"))
        e.printStackTrace()
      }
    }
  }


  def setNavigation() = {
    IdNav(model.get.id).navigation().map{ nav =>
      model.subProp(_.navigation).set(nav)
    }
  }

  private var widget:Widget = new Widget {
    import scalatags.JsDom.all._
    override def render(): JsDom.all.Modifier = div()
  }

  def loadWidgets(f:JSONMetadata) = {
    widget = JSONMetadataRenderer(f, model.subProp(_.data), model.subProp(_.children).get)
    widget
  }

  def next() = IdNav(model.get.id).next(model.get.kind,model.get.name).map(_.map(goTo))
  def prev() = IdNav(model.get.id).prev(model.get.kind,model.get.name).map(_.map(goTo))
  def nextPage() = IdNav(model.get.id).nextPage(model.get.kind,model.get.name, Session.getQuery().get).map(_.map(goTo))
  def prevPage() = IdNav(model.get.id).prevPage(model.get.kind,model.get.name, Session.getQuery().get).map(_.map(goTo))

  def goTo(id:String) = {
    model.subProp(_.loading).set(true)
    val m = model.get
    val newState = Routes(m.kind,m.name).edit(id)
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }


}

case class EntityFormView(model:ModelProperty[EntityFormModel], presenter:EntityFormPresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe.generic.auto._

  override def renderChild(view: View): Unit = {}


  override def getTemplate: scalatags.generic.Modifier[Element] = {

//    val pageNavigation = {
//
//      div(
//        showIf(model.subProp(_.ids.currentPage).transform(_ != 1)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(model.subProp(_.ids.currentPage).get -1), true), Labels.navigation.previous).render },
//        span(
//          " Page: ",
//          bind(model.subProp(_.ids.currentPage)),
//          " of ",
//          bind(model.subProp(_.pages)),
//          " "
//        ),
//        showIf(model.subProp(_.ids.isLastPage).transform(!_)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(model.subProp(_.ids.currentPage).get + 1), true),Labels.navigation.next).render },
//        br,br
//      )
//    }

    val recordNavigation = {

      div(
        showIf(model.subProp(_.navigation.hasPrevious)) { a(onclick :+= ((ev: Event) => presenter.prev(), true), Labels.navigation.previous).render },
        span(
          bind(model.subProp(_.navigation.current)),
          " of ",
          bind(model.subProp(_.navigation.count))
        ),
        showIf(model.subProp(_.navigation.hasNext)) { a(onclick :+= ((ev: Event) => presenter.next(), true),Labels.navigation.next).render },

        br,

        showIf(model.subProp(_.navigation.hasPreviousPage)) { a(onclick :+= ((ev: Event) => presenter.prevPage(), true), Labels.navigation.previousPage).render },
        span("Page ",
          bind(model.subProp(_.navigation.currentPage)),
          " of ",
          bind(model.subProp(_.navigation.pages))
        ),
        showIf(model.subProp(_.navigation.hasNextPage)) { a(onclick :+= ((ev: Event) => presenter.nextPage(), true),Labels.navigation.nextPage).render }
      )
    }


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
        produce(model.subProp(_.name)) { m =>
          div(
            a(href := Routes(model.subProp(_.kind).get, m).entity(m).url)(Labels.entities.table + " " + m)
          ).render
        }
      ),
      recordNavigation,
      produce(model.subProp(_.error)){ error =>
        div(
          if(error.length > 0) {
            UdashLabel.danger(UdashBootstrap.newId(), error).render
          } else {

          }
        ).render
      },
      br,
      hr,
      produce(model.subProp(_.metadata)){ form =>
        div(
          form match {
            case None => p("Loading form")
            case Some(f) => {
              presenter.loadWidgets(f).render()
            }
          }
        ).render
      },
      //save and stay on same record
      button(
        cls := "primary",
        onclick :+= ((ev: Event) => presenter.save((kind,name)=>Routes(kind,name).edit(model.get.id.getOrElse(""))), true)
      )(Labels.form.save),br,
      //save and go to table view
      button(
        cls := "primary",
        onclick :+= ((ev: Event) => presenter.save((kind,name)=>Routes(kind,name).entity()), true)
      )(Labels.form.save_table),br,
      //save and go insert new record
      button(
        cls := "primary",
        onclick :+= ((ev: Event) => presenter.save((kind,name)=>Routes(kind,name).add()), true)
      )(Labels.form.save_add),br,br,

      Debug(model.subProp(_.data), "data"),
      Debug(model.subProp(_.metadata), "metadata")
    )
  }
}
