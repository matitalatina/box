package ch.wsl.box.client.views

import ch.wsl.box.client.ModelTableState
import ch.wsl.box.client.services.Box
import io.circe.Json
import io.udash._
import io.udash.bootstrap.table.UdashTable
import org.scalajs.dom.Element

import scala.util.Try
import scalatags.generic.Modifier

/**
  * Created by andre on 4/24/2017.
  */


case class ModelTableModel(name:String,rows:Seq[Json])

case object ModelTableViewPresenter extends ViewPresenter[ModelTableState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty{
      ModelTableModel("",Seq())
    }

    (ModelTableView(model),ModelTablePresenter(model))
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel]) extends Presenter[ModelTableState]{

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: ModelTableState): Unit = {
    model.subProp(_.name).set(state.model)
    Box.list(state.model).map{ list =>
      model.subSeq(_.rows).set(list)
    }
  }
}

case class ModelTableView(model:ModelProperty[ModelTableModel]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  override def renderChild(view: View): Unit = {}

  override def getTemplate: scalatags.generic.Modifier[Element] = div(
    h1(bind(model.subProp(_.name))),
    UdashTable()(model.subSeq(_.rows))(
      headerFactory = Some(() => {
        tr(
          produce(model.subProp(_.rows)) { rows =>
            for ((k, v) <- rows.headOption.toList.flatMap(_.asObject.get.toList)) yield {
              th(k).render
            }
          }
        ).render
      }),
      rowFactory = (el) => tr(
        for((k,v) <- el.get.asObject.get.toList) yield {
          val content:String = v.as[String].right.getOrElse(v.toString())
          td(content)
        }
      ).render
    ).render
  )
}
