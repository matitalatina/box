package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{EntityFormState, EntityTableState}
import ch.wsl.box.client.services.{Enhancer, REST}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.{Conf, Labels, Session}
import ch.wsl.box.client.views.components.TableFieldsRenderer
import ch.wsl.box.model.shared.EntityKind.VIEW
import ch.wsl.box.model.shared._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.table.UdashTable
import io.udash.properties.single.Property
import org.scalajs.dom

import scalacss.ScalatagsCss._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Element, Event, KeyboardEvent}

import scala.concurrent.Future


case class Row(data: Seq[String])
case class FieldQuery(field:JSONField, sort:String, filter:String, filterType:String)
case class EntityTableModel(name:String, kind:String, rows:Seq[Row], fieldQueries:Seq[FieldQuery],
                            metadata:Option[JSONMetadata], selectedRow:Option[Row], ids: IDs, pages:Int)

object EntityTableModel{
  def empty = EntityTableModel("","",Seq(),Seq(),None,None,IDs(true,1,Seq(),0),1)
}

case class EntityTableViewPresenter(routes:Routes, onSelect:Seq[(JSONField,String)] => Unit = (f => Unit)) extends ViewPresenter[EntityTableState] {

  import ch.wsl.box.client.Context._



  override def create(): (View, Presenter[EntityTableState]) = {

    val model = ModelProperty(EntityTableModel.empty)

    val presenter = EntityTablePresenter(model,onSelect,routes)
    (EntityTableView(model,presenter,routes),presenter)
  }
}


/*
Failed to decode JSON on
/model/en/v_remark_base/metadata
with error: DecodingFailure(String, List(DownArray, DownField(fields), DownArray, DownField(blocks), DownField(layout)))
 */
case class EntityTablePresenter(model:ModelProperty[EntityTableModel], onSelect:Seq[(JSONField,String)] => Unit, routes:Routes) extends Presenter[EntityTableState]{

  import ch.wsl.box.client.Context._
  import Enhancer._

  override def handleState(state: EntityTableState): Unit = {
    model.set(EntityTableModel.empty)
    model.subProp(_.name).set(state.entity)
    model.subProp(_.kind).set(state.kind)

    val emptyJsonQuery = JSONQuery.empty.limit(Conf.pageLength)

    println("handling state")

    for{
      emptyFieldsForm <- REST.metadata(state.kind,Session.lang(),state.entity)
      fields = emptyFieldsForm.fields.filter(field => emptyFieldsForm.tabularFields.contains(field.name))
      filteredForm = emptyFieldsForm.copy(fields = fields)
      lookupEntities <- Enhancer.fetchLookupEntities(Seq(filteredForm))
      form = Enhancer.populateLookupValuesInFields(lookupEntities,filteredForm)

      defaultQuery = form.query match {
        case None => emptyJsonQuery
        case Some(jsonquery) => jsonquery.copy(paging = emptyJsonQuery.paging)   //in case a specific sorting or filtering is specified in box.form
      }

      query = Session.getQuery() match {
        case None => defaultQuery
        case Some(jsonquery) => jsonquery      //in case a query is already stored in Session
      }

      csv <- REST.csv(state.kind,Session.lang(),state.entity,query)
      ids <- REST.ids(model.get.kind,Session.lang(),model.get.name,query)
//      all_ids <- REST.ids(model.get.kind,Session.lang(),model.get.name, JSONQuery.empty.limit(100000))
      specificKind <- REST.specificKind(state.kind, Session.lang(), state.entity)
    } yield {


      val m = EntityTableModel(
        name = state.entity,
        kind = specificKind,
        rows = csv.map(Row(_)),
        fieldQueries = form.fields.map{ field =>
          FieldQuery(
            field = field,
            sort = form.query.flatMap(_.sort.find(_.column == field.name).map(_.order)).getOrElse(Sort.IGNORE),
            filter = form.query.flatMap(_.filter.find(_.column == field.name).map(_.value)).getOrElse(""),
            filterType = form.query.flatMap(_.filter.find(_.column == field.name).flatMap(_.operator)).getOrElse(Filter.default(field.`type`))
          )
        },
        metadata = Some(form),
        selectedRow = None,
        ids = ids,
        pages = pageCount(ids)
      )

      saveIds(ids,query)

      model.set(m)
    }
  }

  private def pageCount(ids: IDs):Int = {
    math.ceil(ids.count.toDouble / Conf.pageLength.toDouble).toInt
  }

  def ids(el:Row): JSONID = Enhancer.extractID(el.data,model.subProp(_.metadata).get.toSeq.flatMap(_.tabularFields),model.subProp(_.metadata).get.toSeq.flatMap(_.keys))

  def edit(el:Row) = {
    val k = ids(el)
    val newState = routes.edit(k.asString)
    io.udash.routing.WindowUrlChangeProvider.changeUrl(newState.url)
  }

  def saveIds(ids: IDs, query:JSONQuery) = {
    Session.setQuery(query)
    Session.setIDs(ids)
  }


  private def query():JSONQuery = {
    val fieldQueries = model.subProp(_.fieldQueries).get
    val sort = fieldQueries.filter(_.sort != Sort.IGNORE).map(s => JSONSort(s.field.name, s.sort)).toList
    val filter = fieldQueries.filter(_.filter != "").map(f => JSONQueryFilter(f.field.name,Some(f.filterType),f.filter)).toList
    JSONQuery(filter, sort, None)
  }

  def reloadRows(page:Int) = {
    println("reloading rows")

    val q = query().copy(paging = Some(JSONQueryPaging(Conf.pageLength,page)))

    for {
      csv <- REST.csv(model.subProp(_.kind).get,Session.lang(),model.subProp(_.name).get,q)
      ids <- REST.ids(model.get.kind,Session.lang(),model.get.name,q)
    } yield {
      model.subProp(_.rows).set(csv.map(Row(_)))
      model.subProp(_.ids).set(ids)
      model.subProp(_.pages).set(pageCount(ids))
      saveIds(ids,q)
    }

  }

  def filterById(id:JSONID) = {
    val newMetadata = model.subProp(_.fieldQueries).get.map{ m =>
      id.id.headOption.exists(_.key == m.field.name) match {
        case true => m.copy(filter = id.id.head.value, filterType = Filter.EQUALS)
        case false => m
      }
    }
    model.subProp(_.fieldQueries).set(newMetadata)
    reloadRows(1)
  }

  def filter(metadata: FieldQuery, filter:String) = {
    println("filtering")
    val newFieldQueries = model.subProp(_.fieldQueries).get.map{ m =>
      m.field.name == metadata.field.name match {
        case true => m.copy(filter = filter)
        case false => m
      }
    }
    model.subProp(_.fieldQueries).set(newFieldQueries)
    reloadRows(1)
  }

  def filterType(fieldQuery:FieldQuery, filterType:String) = {
    println("setting filtertype " + filterType)
    val newFieldQueries = model.subProp(_.fieldQueries).get.map{ m =>
      m.field.name == fieldQuery.field.name match {
        case true => m.copy(filterType = filterType)
        case false => m
      }
    }
    model.subProp(_.fieldQueries).set(newFieldQueries)
    reloadRows(1)
  }

  def sort(fieldQuery: FieldQuery) = {

    val newFieldQueries = model.subProp(_.fieldQueries).get.map{ m =>
      m.field.name == fieldQuery.field.name match {
        case false => m
        case true => m.copy(sort = Sort.next(m.sort))
      }
    }
    model.subProp(_.fieldQueries).set(newFieldQueries)
    reloadRows(1)
  }

  def selected(row: Row) = {
    onSelect(model.get.fieldQueries.map(_.field).zip(row.data))
    model.subProp(_.selectedRow).set(Some(row))
  }

  def nextPage() = {
    if(!model.subProp(_.ids.isLastPage).get) {
      reloadRows(model.subProp(_.ids.currentPage).get + 1)
    }
  }
  def prevPage() = {
    if(model.subProp(_.ids.currentPage).get > 1) {
      reloadRows(model.subProp(_.ids.currentPage).get - 1)
    }
  }

  def downloadCSV() = {
    query().asJson.toString()
    val kind = EntityKind(model.subProp(_.kind).get).entityOrForm
    dom.window.open(s"/api/v1/$kind/${Session.lang()}/${model.subProp(_.name).get}/csv?q=${query().asJson.toString()}")
  }
}

case class EntityTableView(model:ModelProperty[EntityTableModel], presenter:EntityTablePresenter, routes:Routes) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  import Enhancer._

  override def renderChild(view: View): Unit = {}


  def filterOptions(fieldQuery: FieldQuery) = {
    val filterTypeModel = Property(fieldQuery.filterType)
//    println(filterTypeModel.get)
//    println(Filter.>)
//    println(Filter.<)

    //hack using model transformation to get onChange event, using standard HTML breaks udash property model
    val filterTypeHandler = filterTypeModel.transform(
      (s:String) => s,
      {(s:String) =>
        println("changed filter" + s)
        presenter.filterType(fieldQuery,s)  //aggiorna metadata
        s
      }
    )

    Select(filterTypeHandler, Filter.options(fieldQuery.field.`type`), Select.defaultLabel)()

  }

  override def getTemplate: scalatags.generic.Modifier[Element] = {

    val pagination = {

      div(
        showIf(model.subProp(_.ids.currentPage).transform(_ != 1)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(1), true), Labels.navigation.first).render },
        showIf(model.subProp(_.ids.currentPage).transform(_ != 1)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(model.subProp(_.ids.currentPage).get -1), true), Labels.navigation.previous).render },
        span(
          " " + Labels.navigation.page + " ",
          bind(model.subProp(_.ids.currentPage)),
          " " + Labels.navigation.of + " ",
          bind(model.subProp(_.pages)),
          " "
        ),
        showIf(model.subProp(_.ids.isLastPage).transform(!_)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(model.subProp(_.ids.currentPage).get + 1), true),Labels.navigation.next).render },
        showIf(model.subProp(_.ids.isLastPage).transform(!_)) { a(onclick :+= ((ev: Event) => presenter.reloadRows(model.subProp(_.pages).get ), true),Labels.navigation.last).render },
        br,br
      )
    }


    div(
      h3(bind(model.subProp(_.name))),
      div(BootstrapStyles.pullLeft) (
        if (model.get.kind != VIEW.kind)
          a(href := routes.add().url)(Labels.entities.`new` + " ",bind(model.subProp(_.name)))
        else
          p()
      ),
      div(BootstrapStyles.pullRight) (
        pagination.render
      ),

      div(id := "box-table",
//        pagination.render,
        UdashTable()(model.subSeq(_.rows))(
          headerFactory = Some(() => {
              tr(
                th(GlobalStyles.smallCells)(Labels.entity.actions),
//                produce(model.subProp(_.metadata)) { metadata =>
                  produce(model.subProp(_.fieldQueries)) { fieldQueries =>
                  for {
//                    fieldName <- metadata.toSeq.flatMap(_.tabularFields)
//                    fieldQuery <- model.subProp(_.fieldQueries).get.filter(_.field.name == fieldName)
                    fieldQuery <- fieldQueries
                  } yield {
                    val title: String = fieldQuery.field.label.getOrElse(fieldQuery.field.name)
                    val filter = Property(fieldQuery.filter)
                    val sort = Property(fieldQuery.sort)

                    th(GlobalStyles.smallCells)(
                      a(
                        onclick :+= ((ev: Event) => presenter.sort(fieldQuery), true),
                        title," ",
                        Labels(Sort.label(sort.get))
                      ),br,
                      filterOptions(fieldQuery),
                      TextInput.debounced(filter,onkeyup :+= ((ev: KeyboardEvent) => if(ev.keyCode == KeyCode.Enter) presenter.filter(fieldQuery,filter.get), true))

                    ).render
                  }
                }
            ).render
          }),
          rowFactory = (el) => {
            val key = presenter.ids(el.get)

            val selected = model.subProp(_.selectedRow).transform(_.exists(_ == el.get))
            val kind = model.subProp(_.kind).get

            tr((`class` := "info").attrIf(selected), onclick :+= ((e:Event) => presenter.selected(el.get),true),
              td(GlobalStyles.smallCells)(
                kind match{
                  case "view" => p(color := "grey")(Labels.entity.no_action)
                  case _ => a(
                    cls := "primary",
                    onclick :+= ((ev: Event) => presenter.edit(el.get), true)
                  )(Labels.entity.edit)
                }
                ),
              produce(model.subSeq(_.fieldQueries)) { fieldQueries =>
                for {(fieldQuery, i) <- fieldQueries.zipWithIndex} yield {

                  val value = el.get.data.lift(i).getOrElse("")
                  td(GlobalStyles.smallCells)(TableFieldsRenderer(
                    value,
                    fieldQuery.field,
                    key,
                    routes
                  )).render
                }
              }
            ).render
          }
        ).render,
        button(`type` := "button", onclick :+= ((e:Event) => presenter.downloadCSV()),"Download CSV"),
        showIf(model.subProp(_.fieldQueries).transform(_.size == 0)){ p("loading...").render },
        br,br
      )
    )
  }


}
