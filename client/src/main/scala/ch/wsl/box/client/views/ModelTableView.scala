package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{ModelFormState, ModelTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.{Conf, Labels, Session}
import ch.wsl.box.client.views.components.TableFieldsRenderer
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bootstrap.table.UdashTable
import org.scalajs.dom

import scalacss.ScalatagsCss._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Element, Event, KeyboardEvent}

import scala.concurrent.Future




/**
  * Created by andre on 4/24/2017.
  */



case class Row(data: Seq[String])
case class Metadata(field:JSONField,sort:String,filter:String,filterType:String)
case class ModelTableModel(name:String, kind:String, rows:Seq[Row], metadata:Seq[Metadata], form:Option[JSONMetadata], selected:Option[Row], keyList: KeyList, pages:Int)

object ModelTableModel{
  def empty = ModelTableModel("","",Seq(),Seq(),None,None,KeyList(true,1,Seq(),0),1)
}

case class ModelTableViewPresenter(routes:Routes,onSelect:Seq[(JSONField,String)] => Unit = (f => Unit)) extends ViewPresenter[ModelTableState] {

  import ch.wsl.box.client.Context._



  override def create(): (View, Presenter[ModelTableState]) = {

    val model = ModelProperty(ModelTableModel.empty)

    val presenter = ModelTablePresenter(model,onSelect,routes)
    (ModelTableView(model,presenter,routes),presenter)
  }
}


/*
Failed to decode JSON on
/model/en/v_remark_base/metadata
with error: DecodingFailure(String, List(DownArray, DownField(fields), DownArray, DownField(blocks), DownField(layout)))
 */
case class ModelTablePresenter(model:ModelProperty[ModelTableModel], onSelect:Seq[(JSONField,String)] => Unit, routes:Routes) extends Presenter[ModelTableState]{

  import ch.wsl.box.client.Context._
  import Enhancer._

  override def handleState(state: ModelTableState): Unit = {
    model.set(ModelTableModel.empty)
    model.subProp(_.name).set(state.model)
    model.subProp(_.kind).set(state.kind)

   val defaultJsonQuery = JSONQuery.limit(Conf.pageLength)

    for{

      emptyFieldsForm <- REST.form(state.kind,Session.lang(),state.model)
      fields = emptyFieldsForm.fields.filter(field => emptyFieldsForm.tableFields.contains(field.key))
      filteredForm = emptyFieldsForm.copy(fields = fields)
      models <- Enhancer.fetchModels(Seq(filteredForm))
      form = Enhancer.populateOptionsValuesInFields(models,filteredForm)
      query = form.query match {
        case None => defaultJsonQuery
        case Some(jsonquery) => jsonquery.copy(paging = defaultJsonQuery.paging)   //in case a specific sorting or filtering is specified in box.form
      }
      csv <- REST.csv(state.kind,Session.lang(),state.model,query)
      keyList <- REST.keysList(model.get.kind,Session.lang(),model.get.name,query)
    } yield {


      val m = ModelTableModel(
        name = state.model,
        kind = state.kind,
        rows = csv.map{ Row(_)},
        metadata = form.fields.map{ field =>
          Metadata(
            field = field,
            sort = form.query.flatMap(_.sort.find(_.column == field.key).map(_.order)).getOrElse(Sort.IGNORE),
            filter = form.query.flatMap(_.filter.find(_.column == field.key).map(_.value)).getOrElse(""),
            filterType = form.query.flatMap(_.filter.find(_.column == field.key).flatMap(_.operator)).getOrElse(Filter.default(field.`type`))
          )
        },
        form = Some(form),
        None,
        keyList,
        pageCount(keyList)
      )

      saveKeys(keyList,query)

      model.set(m)
    }
  }

  private def pageCount(keyList: KeyList):Int = {
    math.ceil(keyList.count.toDouble / Conf.pageLength.toDouble).toInt
  }

  def key(el:Row) = Enhancer.extractKeys(el.data,model.subProp(_.form).get.toSeq.flatMap(_.tableFields),model.subProp(_.form).get.toSeq.flatMap(_.keys))

  def edit(el:Row) = {
    val k = key(el)
    val newState = routes.edit(k.asString)
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }

  def saveKeys(keyList: KeyList,query:JSONQuery) = {
    Session.setQuery(query)
    Session.setKeys(keyList)
  }

  def reloadRows(page:Int) = {

    val metadata = model.subProp(_.metadata).get
    val sort = metadata.filter(_.sort != Sort.IGNORE).map(s => JSONSort(s.field.key, s.sort)).toList
    val filter = metadata.filter(_.filter != "").map(f => JSONQueryFilter(f.field.key,Some(f.filterType),f.filter)).toList
    val query = JSONQuery(Conf.pageLength, page, sort, filter)

    for {
      csv <- REST.csv(model.subProp(_.kind).get,Session.lang(),model.subProp(_.name).get,query)
      keyList <- REST.keysList(model.get.kind,Session.lang(),model.get.name,query)
    } yield {
      model.subProp(_.rows).set(csv.map(Row(_)))
      model.subProp(_.keyList).set(keyList)
      model.subProp(_.pages).set(pageCount(keyList))
      saveKeys(keyList,query)
    }

  }

  def filterByKey(key:JSONKeys) = {
    val newMetadata = model.subProp(_.metadata).get.map{ m =>
      key.keys.headOption.exists(_.key == m.field.key) match {
        case true => m.copy(filter = key.keys.head.value, filterType = Filter.EQUALS)
        case false => m
      }
    }
    model.subProp(_.metadata).set(newMetadata)
    reloadRows(1)
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
    reloadRows(1)
  }

  def filterType(metadata:Metadata, filterType:String) = {
    println("setting filtertype " + filterType)
    val newMetadata = model.subProp(_.metadata).get.map{ m =>
      m.field.key == metadata.field.key match {
        case true => m.copy(filterType = filterType)
        case false => m
      }
    }
    model.subProp(_.metadata).set(newMetadata)
    reloadRows(1)
  }

  def sort(metadata: Metadata) = {

    val newMetadata = model.subProp(_.metadata).get.map{ m =>
      m.field.key == metadata.field.key match {
        case false => m
        case true => m.copy(sort = Sort.next(m.sort))
      }
    }
    model.subProp(_.metadata).set(newMetadata)
    reloadRows(1)
  }

  def selected(row: Row) = {
    onSelect(model.get.metadata.map(_.field).zip(row.data))
    model.subProp(_.selected).set(Some(row))
  }

  def nextPage() = {
    if(!model.subProp(_.keyList.last).get) {
      reloadRows(model.subProp(_.keyList.page).get + 1)
    }
  }
  def prevPage() = {
    if(model.subProp(_.keyList.page).get > 1) {
      reloadRows(model.subProp(_.keyList.page).get - 1)
    }
  }
}

case class ModelTableView(model:ModelProperty[ModelTableModel],presenter:ModelTablePresenter,routes:Routes) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  import Enhancer._

  override def renderChild(view: View): Unit = {}


  def filterOptions(metadata: Metadata) = {
    val filterTypeModel = Property(metadata.filterType)
    println(filterTypeModel.get)
    println(Filter.>)
    println(Filter.<)

    //hack using model transformation to get onChange event, using standard HTML breaks udash property model
    val filterTypeHandler = filterTypeModel.transform(
      (s:String) => s,
      {(s:String) =>
        println("changed " + s)
        presenter.filterType(metadata,s)  //aggiorna metadata
        s
      }
    )

    Select(filterTypeHandler, Filter.options(metadata.field.`type`), Select.defaultLabel)()

  }

  override def getTemplate: scalatags.generic.Modifier[Element] = {

    val pagination = {

      div(
        showIf(model.subProp(_.keyList.page).transform(_ != 1)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(model.subProp(_.keyList.page).get -1), true), Labels.navigation.previous).render },
        span(
          " Page: ",
          bind(model.subProp(_.keyList.page)),
          " of ",
          bind(model.subProp(_.pages)),
          " "
        ),
        showIf(model.subProp(_.keyList.last).transform(!_)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(model.subProp(_.keyList.page).get + 1), true),Labels.navigation.next).render },
        br,br
      )
    }


    div(
      h1(bind(model.subProp(_.name))),
      div(id := "box-table",
        UdashTable()(model.subSeq(_.rows))(
          headerFactory = Some(() => {
              tr(
                th(GlobalStyles.smallCells)(Labels.table.actions),
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
              )(Labels.table.edit)),
              produce(model.subSeq(_.metadata)) { metadatas =>
                for {(metadata, i) <- metadatas.zipWithIndex} yield {

                  val value = el.get.data.lift(i).getOrElse("")
                  td(GlobalStyles.smallCells)(TableFieldsRenderer(
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
        pagination.render,
        showIf(model.subProp(_.metadata).transform(_.size == 0)){ p("loading...").render }
      )
    )
  }


}
