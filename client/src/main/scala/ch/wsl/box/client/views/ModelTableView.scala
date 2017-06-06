package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.Session
import ch.wsl.box.client.views.components.FieldsRenderer
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bootstrap.form.{InputGroupSize, UdashInputGroup}
import io.udash.bootstrap.table.UdashTable

import scalacss.ScalatagsCss._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Element, Event, KeyboardEvent}

import scala.concurrent.Future


/**
  * Created by andre on 4/24/2017.
  */



case class Row(data: Seq[String])
case class Metadata(field:JSONField,sort:String,filter:String,filterType:String)
case class ModelTableModel(name:String,kind:String,rows:Seq[Row],metadata:Seq[Metadata],form:Option[JSONForm],selected:Option[Row])

object ModelTableModel{
  def empty = ModelTableModel("","",Seq(),Seq(),None,None)
}

case class ModelTableViewPresenter(routes:Routes,onSelect:Seq[(JSONField,String)] => Unit = (f => Unit)) extends ViewPresenter[ModelTableState] {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue



  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty(ModelTableModel.empty)

    val presenter = ModelTablePresenter(model,onSelect,routes)
    (ModelTableView(model,presenter,routes),presenter)
  }
}

case class ModelTablePresenter(model:ModelProperty[ModelTableModel], onSelect:Seq[(JSONField,String)] => Unit, routes:Routes) extends Presenter[ModelTableState]{

  import ch.wsl.box.client.Context._
  import Enhancer._

  override def handleState(state: ModelTableState): Unit = {
    model.set(ModelTableModel.empty)
    model.subProp(_.name).set(state.model)
    model.subProp(_.kind).set(state.kind)

    val query = JSONQuery.limit(30)

    for{
      csv <- REST.csv(state.kind,state.model,query)
      emptyFieldsForm <- REST.form(state.kind,state.model)
      fields = emptyFieldsForm.fields.filter(field => emptyFieldsForm.tableFields.contains(field.key))
      filteredForm = emptyFieldsForm.copy(fields = fields)
      models <- Enhancer.fetchModels(Seq(filteredForm))
      form = Enhancer.populateOptionsValuesInFields(models,filteredForm,Seq())
      _ <- saveKeys(query)
    } yield {


      val m = ModelTableModel(
        name = state.model,
        kind = state.kind,
        rows = csv.map{ Row(_)},
        metadata = form.fields.map{ field =>
          Metadata(field,Sort.IGNORE,"",Filter.default(field.`type`))
        },
        form = Some(form),
        None
      )

      model.set(m)
    }
  }

  def key(el:Row) = Enhancer.extractKeys(el.data,model.subProp(_.form).get.toSeq.flatMap(_.tableFields),model.subProp(_.form).get.toSeq.flatMap(_.keys))

  def edit(el:Row) = {
    val k = key(el)
    val newState = routes.edit(k.asString)
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }

  def saveKeys(query:JSONQuery):Future[Boolean] = {
    Session.setQuery(query)
    REST.keysList(model.get.kind,model.get.name,query).map{ x =>
      Session.setKeys(x)
      true
    }
  }

  def reloadRows() = {

    val metadata = model.subProp(_.metadata).get
    val sort = metadata.filter(_.sort != Sort.IGNORE).map(s => JSONSort(s.field.key, s.sort)).toList
    val filter = metadata.filter(_.filter != "").map(f => JSONQueryFilter(f.field.key,Some(f.filterType),f.filter)).toList
    val query = JSONQuery(20, 1, sort, filter)

    for {
      csv <- REST.csv(model.subProp(_.kind).get,model.subProp(_.name).get,query)
      _ <- saveKeys(query)
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
    println("filtering")
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
    onSelect(model.get.metadata.map(_.field).zip(row.data))
    model.subProp(_.selected).set(Some(row))
  }
}

case class ModelTableView(model:ModelProperty[ModelTableModel],presenter:ModelTablePresenter,routes:Routes) extends View {
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
      div(id := "box-table",
        UdashTable()(model.subSeq(_.rows))(
          headerFactory = Some(() => {
              tr(
                th(GlobalStyles.smallCells)("Actions"),
                produce(model.subProp(_.form)) { form =>
                  for {
                    key <- form.toSeq.flatMap(_.tableFields)
                    metadata <- model.subProp(_.metadata).get.filter(_.field.key == key)
                  } yield {
                    val title: String = metadata.field.title.getOrElse(metadata.field.key)
                    val filter = Property(metadata.filter)

                    th(GlobalStyles.smallCells)(
                      a(
                        onclick :+= ((ev: Event) => presenter.sort(metadata), true),
                        title," ",
                        metadata.sort
                      ),br,
                      filterOptions(metadata),
                      TextInput.debounced(filter,onkeyup :+= ((ev: KeyboardEvent) => if(ev.keyCode == KeyCode.Enter) presenter.filter(metadata,filter.get), true))

                    ).render
                  }
                }
            ).render
          }),
          rowFactory = (el) => {
            val key = presenter.key(el.get)

            val selected = model.subProp(_.selected).transform(_.exists(_ == el.get))

            tr((`class` := "info").attrIf(selected), onclick :+= ((e:Event) => presenter.selected(el.get),true),
              td(GlobalStyles.smallCells)(a(
                cls := "primary",
                onclick :+= ((ev: Event) => presenter.edit(el.get), true)
              )("Edit")),
              produce(model.subSeq(_.metadata)) { metadatas =>
                for {(metadata, i) <- metadatas.zipWithIndex} yield {

                  val value = el.get.data.lift(i).getOrElse("")
                  td(GlobalStyles.smallCells)(FieldsRenderer(
                    value,
                    metadata.field,
                    key,
                    routes
                  )).render
                }
              }
            ).render
          }
        ).render,
        showIf(model.subProp(_.metadata).transform(_.size == 0)){ p("loading...").render }
      )
    )
  }


}
