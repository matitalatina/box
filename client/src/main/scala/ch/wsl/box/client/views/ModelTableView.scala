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


case class ModelTableModel(name:String,rows:Seq[Json], fields:Seq[JSONField], keys:Seq[String], editing:Seq[(String,Seq[(String,Boolean)])])

case object ModelTableViewPresenter extends ViewPresenter[ModelTableState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty{
      ModelTableModel("",Seq(),Seq(),Seq(),Seq())
    }

    val presenter = ModelTablePresenter(model)
    (ModelTableView(model,presenter),presenter)
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel]) extends Presenter[ModelTableState]{

  import ch.wsl.box.client.Context._
  import Enhancer._

  override def handleState(state: ModelTableState): Unit = {
    model.subProp(_.name).set(state.model)
    for{
      list <- REST.list(state.model)
      emptyFields <- REST.form(state.model)
      keys <- REST.keys(state.model)
      fields <- Enhancer.populateOptionsValuesInFields(emptyFields)
    } yield {

      val editing = list.map(el => el.keys(keys).asString -> fields.map{ f =>  f.key -> false })
      val m = ModelTableModel(
        name = state.model,
        rows = list,
        fields = fields,
        keys = keys,
        editing = editing
      )

      model.set(m)
    }
  }



  def edit(el:Json) = {
    val key = el.keys(model.subProp(_.keys).get)
    val newState = ModelFormState(model.subProp(_.name).get,Some(key.asString))
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
            produce(model.subSeq(_.fields)) { fields =>
              for {field <- fields} yield {
                val title: String = field.title.getOrElse(field.key)
                th(title).render
              }
            },
            th("Actions")
          ).render
        }),
        rowFactory = (el) => tr(
          produce(model.subSeq(_.fields)) { fields =>
            for {field <- fields} yield {
              val key = el.get.keys(model.get.keys).asString
              val editing = model.subProp(_.editing).transform(toEditable(field.key,key), fromEditable(model.get.editing, field.key, key))
              td(FieldsRenderer(
                el.get,
                field,
                model.subProp(_.keys).get, el.transform(x => x.get(field.key), x => Enhancer.parse(field, Some(x))(_ => Unit)._2),
                editing
              )).render
            }
          },
          td(button(
            cls := "primary",
            onclick :+= ((ev: Event) => presenter.edit(el.get), true)
          )("Edit"))
        ).render
      ).render
  )

  def toEditable(field:String,key:String)(in:Seq[(String,Seq[(String,Boolean)])]):Boolean = {

    val result = for{
      field <- in.find(_._1 == key).map(_._2)
      result <- field.find(_._2 == field).map(_._2)
    } yield {
      result
    }

    result.getOrElse(false)
  }

  def fromEditable(original:Seq[(String,Seq[(String,Boolean)])],field:String, key:String)(in:Boolean):Seq[(String,Seq[(String,Boolean)])] = original.map{case (k,v) =>
    val value = if(k == key) {
      v.map{ case (k2,v2) =>
        val result = if(k2 == field) {
          in
        } else {
          v2
        }
        k2 -> result
      }
    } else v
    k -> value
  }

}
