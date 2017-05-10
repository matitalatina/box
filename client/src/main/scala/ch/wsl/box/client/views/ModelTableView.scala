package ch.wsl.box.client.views

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.views.components.FieldsRenderer
import ch.wsl.box.model.shared.{JSONField, JSONKeys}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.table.UdashTable
import org.scalajs.dom.{Element, Event}

import scala.util.Try
import scalatags.generic.Modifier

/**
  * Created by andre on 4/24/2017.
  */


case class Row(data: Seq[String])
case class ModelTableModel(name:String,rows:Seq[Row], fields:Seq[JSONField], keys:Seq[String])

object ModelTableModel{
  def empty = ModelTableModel("",Seq(),Seq(),Seq())
}

case object ModelTableViewPresenter extends ViewPresenter[ModelTableState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty(ModelTableModel.empty)

    val presenter = ModelTablePresenter(model)
    (ModelTableView(model,presenter),presenter)
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel]) extends Presenter[ModelTableState]{

  import ch.wsl.box.client.Context._
  import Enhancer._

  override def handleState(state: ModelTableState): Unit = {
    model.set(ModelTableModel.empty)
    model.subProp(_.name).set(state.model)
    for{
      list <- REST.list(state.model,20)
      csv <- REST.csv(state.model,50)
      emptyFields <- REST.form(state.model)
      keys <- REST.keys(state.model)
      fields <- Enhancer.populateOptionsValuesInFields(emptyFields)
    } yield {


      val m = ModelTableModel(
        name = state.model,
        rows = csv.map{Row(_)},
        fields = fields,
        keys = keys
      )

      model.set(m)
    }
  }


  def key(el:Row) = Enhancer.extractKeys(el.data,model.subProp(_.fields).get,model.subProp(_.keys).get)

  def edit(el:Row) = {
    val k = key(el)
    val newState = ModelFormState(model.subProp(_.name).get,Some(k.asString))
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }
}

case class ModelTableView(model:ModelProperty[ModelTableModel],presenter:ModelTablePresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  import Enhancer._

  override def renderChild(view: View): Unit = {}


  override def getTemplate: scalatags.generic.Modifier[Element] = div(
    h1(bind(model.subProp(_.name))),
      UdashTable()(model.subSeq(_.rows))(
        headerFactory = Some(() => {
          tr(
            th("Actions"),
            produce(model.subSeq(_.fields)) { fields =>
              for {field <- fields} yield {
                val title: String = field.title.getOrElse(field.key)
                th(title).render
              }
            }

          ).render
        }),
        rowFactory = (el) => {
          val key = presenter.key(el.get)
          tr(
            td(button(
              cls := "primary",
              onclick :+= ((ev: Event) => presenter.edit(el.get), true)
            )("Edit")),
            produce(model.subSeq(_.fields)) { fields =>
              for {(field, i) <- fields.zipWithIndex} yield {
                val value = el.get.data.lift(i).getOrElse("")
                td(FieldsRenderer(
                  value,
                  field,
                  key
                )).render
              }
            }
          ).render
        }
      ).render,
    showIf(model.subProp(_.fields).transform(_.size == 0)){ p("loading...").render }

  )


}
