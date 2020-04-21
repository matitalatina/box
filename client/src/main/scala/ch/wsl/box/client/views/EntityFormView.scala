package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{EntityFormState, EntityTableState}
import ch.wsl.box.client.services.{Enhancer, Navigate, Notification, REST}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils._
import ch.wsl.box.client.views.components.widget.Widget
import ch.wsl.box.client.views.components.{Debug, JSONMetadataRenderer}
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash.{showIf, _}
import io.udash.bootstrap.{BootstrapStyles, UdashBootstrap}
import io.udash.bootstrap.label.UdashLabel
import io.udash.component.ComponentId
import io.udash.core.Presenter
import io.udash.properties.single.Property
import org.scalajs.dom._
import scribe.Logging

import scala.concurrent.Future
import scalatags.JsDom
import scalacss.ScalatagsCss._

import scala.scalajs.js.URIUtils
import scala.language.reflectiveCalls

/**
  * Created by andre on 4/24/2017.
  */

case class EntityFormModel(name:String, kind:String, id:Option[String], metadata:Option[JSONMetadata], data:Json,
                           error:String, children:Seq[JSONMetadata], navigation: Navigation, loading:Boolean, changed:Boolean, write:Boolean)

object EntityFormModel extends HasModelPropertyCreator[EntityFormModel] {
  implicit val blank: Blank[EntityFormModel] =
    Blank.Simple(EntityFormModel("","",None,None,Json.Null,"",Seq(), Navigation.empty0,true,false, true))
}

object EntityFormViewPresenter extends ViewFactory[EntityFormState] {

  import ch.wsl.box.client.Context._
  override def create(): (View, Presenter[EntityFormState]) = {
    val model = ModelProperty.blank[EntityFormModel]
    val presenter = EntityFormPresenter(model)
    (EntityFormView(model,presenter),presenter)
  }
}

case class EntityFormPresenter(model:ModelProperty[EntityFormModel]) extends Presenter[EntityFormState] with Logging {
    import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JSONUtils._

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


    val jsonId = state.id.flatMap(JSONID.fromString)

    {for{
      metadata <- if(reloadMetadata) REST.metadata(state.kind, Session.lang(), state.entity) else Future.successful(model.get.metadata.get)
      children <- if(Seq(EntityKind.FORM,EntityKind.BOX).map(_.kind).contains(state.kind) && reloadMetadata) REST.children(state.kind,state.entity,Session.lang()) else Future.successful(Seq())
      currentData <- state.id match {
        case Some(id) => REST.get(state.kind, Session.lang(), state.entity,jsonId.get)
        case None => Future.successful{
          Json.obj(JSONMetadata.jsonPlaceholder(metadata,children).toSeq :_*)
        }
      }
    } yield {


      //initialise an array of n strings, where n is the number of fields
      val results:Seq[(String,Json)] = Enhancer.extract(currentData, metadata)


      model.set(EntityFormModel(
        name = state.entity,
        kind = state.kind,
        id = state.id,
        metadata = Some(metadata),
        data = currentData,
        "",
        children,
        Navigation.empty1,
        true,
        false,
        state.writeable
      ))




      //need to be called after setting data because we are listening for data changes
      enableGoAway

      setNavigation()

      widget.afterRender()

      model.subProp(_.loading).set(false)

    }}.recover{ case e => e.printStackTrace() }

  }

  import io.circe.syntax._
  import ch.wsl.box.client._

  def save(action:() => Unit) = {

    val m = model.get
    m.metadata.foreach{ metadata =>
//      val jsons = for {
//        (field, i) <- form.fields.zipWithIndex
//      } yield Enhancer.parse(field, m.results.lift(i).map(_._2),form.keys){ t =>
//        model.subProp(_.error).set(s"Error parsing ${field.key} field: " + t.getMessage)
//      }

      val data:Json = m.data

      def saveAction(data:Json) = {
        for {
          id <- JSONID.fromString(m.id.getOrElse("")) match {
            case Some (id) => REST.update (m.kind, Session.lang(), m.name, id, data)
            case None => REST.insert (m.kind, Session.lang (), m.name, data)
          }
          result <- REST.get(m.kind, Session.lang(), m.name, id)
        } yield result

      }

      println(s"data: $data")


      {for{
        updatedData <- widget.beforeSave(data,metadata)
        resultSaved <- saveAction(updatedData)
        _ <- widget.afterSave(resultSaved,metadata)
      } yield {
//        val newState =  Routes(m.kind,m.name).table()
        val newId = JSONID.fromMap(metadata.keys.map(k => (k,resultSaved.js(k))))
        model.subProp(_.id).set(Some(newId.asString))
        model.subProp(_.data).set(resultSaved)
        enableGoAway
        Navigate.to(Routes(model.get.kind, model.get.name).edit(model.get.id.getOrElse("")))
        action()

      }}.recover{ case e =>
        e.getStackTrace.foreach(x => logger.error(s"file ${x.getFileName}.${x.getMethodName}:${x.getLineNumber}"))
        e.printStackTrace()
      }
    }
  }

  def delete() = {

    val confim = window.confirm(Labels.entity.confirmDelete)
    if(confim) {
      for{
        name <- model.get.metadata.map(_.name)
        key <- model.get.id.flatMap(JSONID.fromString)
      } yield {
        REST.delete(model.get.kind, Session.lang(),name,key).map{ count =>
          Notification.add("Deleted " + count.count + " rows")
          Navigate.to(Routes(model.get.kind, name).entity(name))
        }
      }
    }
  }

  def duplicate() = {
    val oldModel = model.get
    model.set(oldModel.copy(
      id = None,
      data = oldModel.metadata.map{ metadata =>
        metadata.keys.foldLeft(oldModel.data)((data,key) => data.hcursor.downField(key).delete.top.get) //removes key from json
      }.getOrElse(oldModel.data)
    ))

  }


  def setNavigation() = {
    Navigator(model.get.id).navigation().map{ nav =>
      model.subProp(_.navigation).set(nav)
    }
  }

  private var widget:Widget = new Widget {
    import scalatags.JsDom.all._
    override protected def show(): JsDom.all.Modifier = div()
    override protected def edit(): JsDom.all.Modifier = div()
  }

  def loadWidgets(f:JSONMetadata) = {
    widget = JSONMetadataRenderer(f, model.subProp(_.data), model.subProp(_.children).get)
    widget
  }


  def navigate(n: ch.wsl.box.client.utils.Navigator => Future[Option[String]]) = {
    widget.killWidget()
    n(nav).map(_.map(goTo))
  }

  def next() = navigate(_.next())
  def prev() = navigate(_.previous())
  def first() = navigate(_.first())
  def last() = navigate(_.last())
  def nextPage() = navigate(_.nextPage())
  def prevPage() = navigate(_.prevPage())
  def firstPage() = navigate(_.firstPage())
  def lastPage() = navigate(_.lastPage())

  def nav = Navigator(model.get.id, model.get.kind,model.get.name)

  def goTo(id:String) = {
    model.subProp(_.loading).set(true)
    val m = model.get
    val r = Routes(m.kind,m.name)
    val newState = if(model.get.write) {
      r.edit(id)
    } else {
      r.show(id)
    }
    Navigate.to(newState)
  }

  model.subProp(_.data).listen { _ =>
    avoidGoAway
  }

  def avoidGoAway = {
    Navigate.disable{ () =>
      window.confirm(Labels.navigation.goAway)
    }
    model.subProp(_.changed).set(true)
    window.onbeforeunload = { (e:BeforeUnloadEvent) =>
      Labels.navigation.goAway
    }
  }
  def enableGoAway = {
    Navigate.enable()
    model.subProp(_.changed).set(false)
    window.onbeforeunload = { (e:BeforeUnloadEvent) =>

    }
  }




}

case class EntityFormView(model:ModelProperty[EntityFormModel], presenter:EntityFormPresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe.generic.auto._
  import io.udash.css.CssView._


  def labelTitle = produce(model.subProp(_.metadata)) { m =>
    val name = m.map(_.label).getOrElse(model.get.name)
    span(name).render
  }

  override def getTemplate: scalatags.generic.Modifier[Element] = {

    val recordNavigation = {

      def navigation = model.subModel(_.navigation)

      div(
        div(ClientConf.style.boxNavigationLabel,
          Navigation.button(navigation.subProp(_.hasPreviousPage),presenter.firstPage,Labels.navigation.firstPage,_.pullLeft),
          Navigation.button(navigation.subProp(_.hasPreviousPage),presenter.prevPage,Labels.navigation.previousPage,_.pullLeft),
          span(
            ClientConf.style.boxNavigationLabel,
            " " + Labels.navigation.page + " ",
            bind(model.subProp(_.navigation.currentPage)),
            " " + Labels.navigation.of + " ",
            bind(model.subProp(_.navigation.pages)),
            " "
          ),
          Navigation.button(navigation.subProp(_.hasNextPage),presenter.lastPage,Labels.navigation.lastPage,_.pullRight),
          Navigation.button(navigation.subProp(_.hasNextPage),presenter.nextPage,Labels.navigation.nextPage,_.pullRight)
        ),
        div(BootstrapStyles.Visibility.clearfix),
        div(ClientConf.style.boxNavigationLabel,
          Navigation.button(navigation.subProp(_.hasPrevious),presenter.first,Labels.navigation.first,_.pullLeft),
          Navigation.button(navigation.subProp(_.hasPrevious),presenter.prev,Labels.navigation.previous,_.pullLeft),
          span(
            " " + Labels.navigation.record + " ",
            bind(model.subModel(_.navigation).subProp(_.currentIndex)),
            " " + Labels.navigation.of + " ",
            bind(model.subModel(_.navigation).subProp(_.count)),
            " "
          ),
          Navigation.button(navigation.subProp(_.hasNext),presenter.last,Labels.navigation.last,_.pullRight),
          Navigation.button(navigation.subProp(_.hasNext),presenter.next,Labels.navigation.next,_.pullRight)
        )
      )
    }


    div(
      div(BootstrapStyles.pullLeft,
        h3(
          ClientConf.style.noMargin,
          labelTitle,
          produce(model.subProp(_.id)){ id =>
            val subTitle = id.map(" - " + _).getOrElse("")
            small(subTitle).render
          },
          showIf(model.subProp(_.loading)) {
            small(" - " + Labels.navigation.loading).render
          },
          showIf(model.subProp(_.changed)) {
            small(style := "color: red"," - " + Labels.form.changed).render
          }

        )
      ),
      div(BootstrapStyles.pullRight,ClientConf.style.navigatorArea) (
        recordNavigation
      ),
      div(BootstrapStyles.pullRight,ClientConf.style.navigatorArea) (
        produceWithNested(model.subProp(_.name)) { (m,release) =>
          div(
            button(ClientConf.style.boxButton,Navigate.click(Routes(model.subProp(_.kind).get, m).entity(m)))(Labels.entities.table + " ", release(labelTitle))," "
          ).render
        }
      ),
      div(BootstrapStyles.Visibility.clearfix),
      produceWithNested(model.subProp(_.write)) { (w,realeser) =>
        if(!w) Seq() else
        div(BootstrapStyles.pullLeft)(
          realeser(produce(model.subProp(_.name)) { m =>
            div(

              //save and stay on same record
              button(
                ClientConf.style.boxButtonImportant,
                onclick :+= ((ev: Event) => presenter.save(() => Unit), true)
              )(Labels.form.save), " ",
              //save and go to table view
              button(
                ClientConf.style.boxButton,ClientConf.style.noMobile,
                onclick :+= ((ev: Event) => presenter.save(() => Navigate.to(Routes(model.get.kind, model.get.name).entity())), true)
              )(Labels.form.save_table), " ",
              //save and go insert new record
              button(
                ClientConf.style.boxButton,ClientConf.style.noMobile,
                onclick :+= ((ev: Event) => presenter.save(() => Navigate.to(Routes(model.subProp(_.kind).get, m).add())), true)
              )(Labels.form.save_add), " ",
              button(ClientConf.style.boxButtonImportant, Navigate.click(Routes(model.subProp(_.kind).get, m).add()))(Labels.entities.`new`), " ",
              button(ClientConf.style.boxButton, onclick :+= ((ev:Event) => presenter.duplicate()))(Labels.entities.duplicate), " ",
              button(ClientConf.style.boxButtonDanger, onclick :+= ((e: Event) => presenter.delete()))(Labels.entity.delete)
            ).render
          })
        ).render
      },
      div(BootstrapStyles.Visibility.clearfix),
      produce(model.subProp(_.error)){ error =>
        div(
          if(error.length > 0) {
            UdashLabel.danger(ComponentId.newId(), error).render
          } else {

          }
        ).render
      },
      hr(ClientConf.style.hrThin),
      produce(model.subProp(_.metadata)){ form =>
        div(BootstrapCol.md(12),ClientConf.style.fullHeightMax,
          form match {
            case None => p("Loading form")
            case Some(f) => {
              presenter.loadWidgets(f).render(model.get.write,Property(true))
            }
          }
        ).render
      },br,br,

      Debug(model.subProp(_.data),b => b, "data"),
      Debug(model.subProp(_.metadata),b => b, "metadata")
    )
  }
}
