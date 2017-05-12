package ch.wsl.box.client.views

import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.views.components.FieldsRenderer
import ch.wsl.box.model.shared._
import io.udash._
import io.udash.bootstrap.form.{InputGroupSize, UdashInputGroup}
import io.udash.bootstrap.table.UdashTable
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Element, Event, KeyboardEvent}


/**
  * Created by andre on 4/24/2017.
  */



case class Row(data: Seq[String])
case class Metadata(field:JSONField,sort:String,filter:String,filterType:String)
case class ModelTableModel(name:String,rows:Seq[Row],keys:Seq[String],metadata:Seq[Metadata],selected:Option[Row])

object ModelTableModel{
  def empty = ModelTableModel("",Seq(),Seq(),Seq(),None)
}

case class ModelTableViewPresenter(onSelect:(Seq[(JSONField,String)],Seq[String]) => Unit = ((f,k) => Unit)) extends ViewPresenter[ModelTableState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty(ModelTableModel.empty)

    val presenter = ModelTablePresenter(model,onSelect)
    (ModelTableView(model,presenter),presenter)
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel], onSelect:(Seq[(JSONField,String)],Seq[String]) => Unit) extends Presenter[ModelTableState]{

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
          Metadata(field,Sort.IGNORE,"",Filter.default(field.`type`))
        },
        None
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

    val metadata = model.subProp(_.metadata).get
    val sort = metadata.filter(_.sort != Sort.IGNORE).map(s => JSONSort(s.field.key, s.sort)).toList
    val filter = metadata.filter(_.filter != "").map(f => JSONQueryFilter(f.field.key,Some(f.filterType),f.filter)).toList
    val query = JSONQuery(20, 1, sort, filter)

    for {
      csv <- REST.csv(model.subProp(_.name).get,query)
    } yield model.subProp(_.rows).set(csv.map(Row(_)))
  }

  def filterByKey(key:JSONKeys) = {
    val newMetadata = model.subProp(_.metadata).get.map{ m =>
      key.keys.headOption.exists(_.key == m.field.key) match {
        case true => m.copy(filter = key.keys.head.value, filterType = Filter.EQUALS)
        case false => m
      }
    }
    model.subProp(_.metadata).set(newMetadata)
    reloadRows()
  }

  def filter(metadata: Metadata,filter:String) = {
    val newMetadata = model.subProp(_.metadata).get.map{ m =>
      m.field.key == metadata.field.key match {
        case true => m.copy(filter = filter)
        case false => m
      }
    }
    model.subProp(_.metadata).set(newMetadata)
    reloadRows()
  }

  def filterType(metadata:Metadata,filterType:String) = {
    val newMetadata = model.subProp(_.metadata).get.map{ m =>
      m.field.key == metadata.field.key match {
        case true => m.copy(filterType = filterType)
        case false => m
      }
    }
    model.subProp(_.metadata).set(newMetadata)
    reloadRows()
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

  def selected(row: Row) = {
    onSelect(model.get.metadata.map(_.field).zip(row.data),model.get.keys)
    model.subProp(_.selected).set(Some(row))
  }
}

case class ModelTableView(model:ModelProperty[ModelTableModel],presenter:ModelTablePresenter) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  import Enhancer._

  override def renderChild(view: View): Unit = {}


  def filterOptions(metadata: Metadata) = {
    val filterModel = Property(metadata.filterType)


    //hack using model transfomation to get onChange event, using standard HTML breaks udash property model
    val filterHandler = filterModel.transform(
      (s:String) => s,
      {(s:String) =>
        println("changed " + s);
        presenter.filterType(metadata,s);
        s
      }
    )

    Select(filterHandler,Filter.options(metadata.field.`type`))

  }

  override def getTemplate: scalatags.generic.Modifier[Element] = {
    div(
      h1(bind(model.subProp(_.name))),
      UdashTable()(model.subSeq(_.rows))(
        headerFactory = Some(() => {
          tr(
            th("Actions"),
            produce(model.subSeq(_.metadata)) { metadataList =>
              for {(metadata) <- metadataList} yield {
                val title: String = metadata.field.title.getOrElse(metadata.field.key)
                val filter = Property(metadata.filter)

                th(
                  a(
                    onclick :+= ((ev: Event) => presenter.sort(metadata), true),
                    title," ",
                    metadata.sort
                  ),
                  br,
                  UdashInputGroup(InputGroupSize.Small)(
                    UdashInputGroup.addon(filterOptions(metadata)),
                    UdashInputGroup.input(TextInput.debounced(filter,onkeyup :+= ((ev: KeyboardEvent) => if(ev.keyCode == KeyCode.Enter) presenter.filter(metadata,filter.get), true)).render)
                  ).render
                ).render
              }
            }

          ).render
        }),
        rowFactory = (el) => {
          val key = presenter.key(el.get)

          val selected = model.subProp(_.selected).transform(_.exists(_ == el.get))

          tr((`class` := "info").attrIf(selected), onclick :+= ((e:Event) => presenter.selected(el.get),true),
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


}
