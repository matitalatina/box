package ch.wsl.box.client.views

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.views.components.FieldsRenderer
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash._
import io.udash.bootstrap.table.UdashTable
import org.scalajs.dom.{Element, Event}

import scala.util.Try
import scalatags.generic.Modifier

/**
  * Created by andre on 4/24/2017.
  */


case class ModelTableModel(name:String,rows:Seq[Json], fields:Seq[JSONField])

case object ModelTableViewPresenter extends ViewPresenter[ModelTableState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty{
      ModelTableModel("",Seq(),Seq())
    }

    val presenter = ModelTablePresenter(model)
    (ModelTableView(model,presenter),presenter)
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel]) extends Presenter[ModelTableState]{

  import ch.wsl.box.client.Context._

  override def handleState(state: ModelTableState): Unit = {
    model.subProp(_.name).set(state.model)
    for{
      list <- REST.list(state.model)
      emptyFields <- REST.form(state.model)
      fields <- Enhancer.populateOptionsValuesInFields(emptyFields)
    } yield {
      model.subSeq(_.fields).set(fields)
      model.subSeq(_.rows).set(list)
    }
  }

  def edit(el:Json) = {
    val id = el.hcursor.get[String]("id").right.getOrElse(el.hcursor.get[Json]("id").right.getOrElse(Json.Null).toString())
    val newState = ModelFormState(model.subProp(_.name).get,Some(id))
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }
}

case class ModelTableView(model:ModelProperty[ModelTableModel],presenter:ModelTablePresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = div(
    h1(bind(model.subProp(_.name))),
    UdashTable()(model.subSeq(_.rows))(
      headerFactory = Some(() => {
        tr(
          produce(model.subSeq(_.fields)) { fields =>
              for{field <- fields} yield {
                val title:String = field.title.getOrElse(field.key)
                th(title).render
              }
          },
          th("Actions")
        ).render
      }),
      rowFactory = (el) => tr(
        produce(model.subSeq(_.fields)) { fields =>
          for{field <- fields} yield {
            td(FieldsRenderer(el.get,field)).render
          }
        },
        td(button(
          cls := "primary",
          onclick :+= ((ev: Event) => presenter.edit(el.get), true)
        )("Edit"))
      ).render
    ).render
  )
}
