package ch.wsl.box.client.views

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.views.components.FieldsRenderer
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bootstrap.form.UdashForm
import io.udash.bootstrap.table.UdashTable
import org.scalajs.dom.{Element, Event}

import scala.util.Try
import scalatags.generic.Modifier

/**
  * Created by andre on 4/24/2017.
  */



case class Row(data: Seq[String])
case class Metadata(field:JSONField,sort:String,filter:String,filterType:String)
case class ModelTableModel(name:String,rows:Seq[Row],keys:Seq[String],metadata:Seq[Metadata])

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
      csv <- REST.csv(state.model,JSONQuery.limit(30))
      emptyFields <- REST.form(state.model)
      keys <- REST.keys(state.model)
      fields <- Enhancer.populateOptionsValuesInFields(emptyFields)
    } yield {


      val m = ModelTableModel(
        name = state.model,
        rows = csv.map{ Row(_)},
        keys = keys,
        metadata = fields.map{ field =>
          Metadata(field,Sort.IGNORE,"",Filter.NONE)
        }
      )

      model.set(m)
    }
  }


  def key(el:Row) = Enhancer.extractKeys(el.data,model.subProp(_.metadata).get.map(_.field),model.subProp(_.keys).get)

  def edit(el:Row) = {
    val k = key(el)
    val newState = ModelFormState(model.subProp(_.name).get,Some(k.asString))
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }

  def reloadRows() = {
    val sort = model.subProp(_.metadata).get.filter(_.sort != Sort.IGNORE).map(s => JSONSort(s.field.key, s.sort)).toList
    val query = JSONQuery(20, 1, sort, List())

    for {
      csv <- REST.csv(model.subProp(_.name).get,query)
    } yield model.subProp(_.rows).set(csv.map(Row(_)))
  }

  def sort(metadata: Metadata) = {

    val newMetadata = model.subProp(_.metadata).get.map{ m =>
      m.field.key == metadata.field.key match {
        case false => m
        case true => m.copy(sort = Sort.next(m.sort))
      }
    }
    model.subProp(_.metadata).set(newMetadata)
    reloadRows()
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
            produce(model.subSeq(_.metadata)) { metadataList =>
              for {(metadata) <- metadataList} yield {
                val title: String = metadata.field.title.getOrElse(metadata.field.key)
                val filter = Property("")
                th(
                  a(
                    onclick :+= ((ev: Event) => presenter.sort(metadata), true),
                    title," ",
                    metadata.sort
                  ),
                  br,
                  UdashForm.textInput()()(filter)).render
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
            produce(model.subSeq(_.metadata)) { metadatas =>
              for {(metadata, i) <- metadatas.zipWithIndex} yield {
                val value = el.get.data.lift(i).getOrElse("")
                td(FieldsRenderer(
                  value,
                  metadata.field,
                  key
                )).render
              }
            }
          ).render
        }
      ).render,
    showIf(model.subProp(_.metadata).transform(_.size == 0)){ p("loading...").render }

  )


}
