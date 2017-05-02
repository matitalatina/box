package ch.wsl.box.client.views

import ch.wsl.box.client.ModelTableState
import ch.wsl.box.client.services.Box
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash._
import io.udash.bootstrap.table.UdashTable
import org.scalajs.dom.Element

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

    (ModelTableView(model),ModelTablePresenter(model))
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel]) extends Presenter[ModelTableState]{

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def handleState(state: ModelTableState): Unit = {
    model.subProp(_.name).set(state.model)
    for{
      list <- Box.list(state.model)
      fields <- Box.form(state.model)
    } yield {
      model.subSeq(_.fields).set(fields)
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
          produce(model.subSeq(_.fields)) { fields =>
              for{field <- fields} yield {
                val title:String = field.title.getOrElse(field.key)
                th(title).render
              }
          }
        ).render
      }),
      rowFactory = (el) => tr(
        produce(model.subSeq(_.fields)) { fields =>
          for{field <- fields} yield {
            val content:String = el.get.hcursor.get[Json](field.key).fold({x => println(x); ""},{x => x.as[String].right.getOrElse(x.toString())})
            td(content).render
          }
        }
      ).render
    ).render
  )
}
