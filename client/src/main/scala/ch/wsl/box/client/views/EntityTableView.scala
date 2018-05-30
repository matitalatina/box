package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{EntityFormState, EntityTableState}
import ch.wsl.box.client.services.{Enhancer, Navigate, Notification, REST}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.{Conf, Labels, Session}
import ch.wsl.box.client.views.components.widget.DateTimeWidget
import ch.wsl.box.client.views.components.{Debug, TableFieldsRenderer}
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
import org.scalajs.dom.{Element, Event, KeyboardEvent, window}
import scalacss.internal.Pseudo.Lang
import scribe.Logging

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
case class EntityTablePresenter(model:ModelProperty[EntityTableModel], onSelect:Seq[(JSONField,String)] => Unit, routes:Routes) extends Presenter[EntityTableState] with Logging {

  import ch.wsl.box.client.Context._
  import Enhancer._

  override def handleState(state: EntityTableState): Unit = {
    model.set(EntityTableModel.empty)
    model.subProp(_.name).set(state.entity)
    model.subProp(_.kind).set(state.kind)

    val emptyJsonQuery = JSONQuery.empty.limit(Conf.pageLength)

    logger.info("handling state")

    for{
      emptyFieldsForm <- REST.metadata(state.kind,Session.lang(),state.entity)
      fields = emptyFieldsForm.fields.filter(field => emptyFieldsForm.tabularFields.contains(field.name))
      form = emptyFieldsForm.copy(fields = fields)
      //lookupEntities <- Enhancer.fetchLookupEntities(Seq(filteredForm))
      //form = Enhancer.populateLookupValuesInFields(lookupEntities,filteredForm)

      defaultQuery:JSONQuery = form.query match {
        case None => emptyJsonQuery
        case Some(jsonquery) => jsonquery.copy(paging = emptyJsonQuery.paging)   //in case a specific sorting or filtering is specified in box.form
      }

      query:JSONQuery = Session.getQuery() match {
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
        fieldQueries = form.tabularFields.flatMap(x => form.fields.find(_.name == x)).map{ field =>
          FieldQuery(
            field = field,
            sort = form.query.flatMap(_.sort.find(_.column == field.name).map(_.order)).getOrElse(Sort.IGNORE),
            filter = form.query.flatMap(_.filter.find(_.column == field.name).map(_.value)).getOrElse(""),
            filterType = form.query.flatMap(_.filter.find(_.column == field.name).flatMap(_.operator)).getOrElse(Filter.default(field))
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
    Navigate.to(newState)
  }

  def delete(el:Row) = {
    val k = ids(el)
    val confim = window.confirm(Labels.entity.confirmDelete)
    if(confim) {
      model.get.metadata.map(_.entity).foreach { entity =>
        REST.delete(model.get.kind, Session.lang(),entity,k).map{ count =>
          Notification.add("Deleted " + count.count + " rows")
          reloadRows(model.get.ids.currentPage)
        }
      }
    }
  }

  def saveIds(ids: IDs, query:JSONQuery) = {
    Session.setQuery(query)
    Session.setIDs(ids)
  }


  private def query():JSONQuery = {
    val fieldQueries = model.subProp(_.fieldQueries).get
    val sort = fieldQueries.filter(_.sort != Sort.IGNORE).map(s => JSONSort(s.field.name, s.sort)).toList
    val filter = fieldQueries.filter(_.filter != "").map(f => JSONQueryFilter(f.field.name,Some(f.filterType),f.filter,f.field.lookup)).toList
    JSONQuery(filter, sort, None)
  }

  def reloadRows(page:Int): Future[Unit] = {
    logger.info("reloading rows")

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

  private var filterUpdateHandler: Int = 0

  model.subProp(_.fieldQueries).listen{ fq =>
    if (filterUpdateHandler != 0) window.clearTimeout(filterUpdateHandler)

    filterUpdateHandler = window.setTimeout(() => {
      reloadRows(1)
    }, 500)
  }


  def filter(metadata: FieldQuery, filter:String) = {
    logger.info("filtering")
    val newFieldQueries = model.subProp(_.fieldQueries).get.map{ m =>
      m.field.name == metadata.field.name match {
        case true => m.copy(filter = filter)
        case false => m
      }
    }
    model.subProp(_.fieldQueries).set(newFieldQueries)
  }

  def sort(fieldQuery: FieldQuery) = {

    val newFieldQueries = model.subProp(_.fieldQueries).get.map{ m =>
      m.field.name == fieldQuery.field.name match {
        case false => m
        case true => m.copy(sort = Sort.next(m.sort))
      }
    }
    model.subProp(_.fieldQueries).set(newFieldQueries)
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
    val (kind,modelName) = model.get.metadata.flatMap(_.exportView) match {
      case Some(view) => ("entity",view)
      case None => (EntityKind(model.subProp(_.kind).get).entityOrForm,model.subProp(_.name).get)
    }


    val url = s"/api/v1/$kind/${Session.lang()}/${modelName}/csv?q=${query().asJson.toString()}".replaceAll("\n","")
    logger.info(s"downloading: $url")
    dom.window.open(url)
  }
}

case class EntityTableView(model:ModelProperty[EntityTableModel], presenter:EntityTablePresenter, routes:Routes) extends View with Logging {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import ch.wsl.box.shared.utils.JsonUtils._

  import Enhancer._

  override def renderChild(view: View): Unit = {}


  def labelTitle = produce(model.subProp(_.metadata)) { m =>
    val name = m.map(_.label).getOrElse(model.get.name)
    span(name).render
  }

  def filterOptions(fieldQuery: ModelProperty[FieldQuery]) = {


    def label = (id:String) => id match {
      case Filter.FK_NOT => StringFrag("not")
      case Filter.FK_EQUALS => StringFrag("=")
      case Filter.FK_LIKE => StringFrag("contains")
      case Filter.LIKE => StringFrag("contains")
      case _ => StringFrag(id)
    }

    Select(fieldQuery.subProp(_.filterType), Filter.options(fieldQuery.get.field),label)(GlobalStyles.fullWidth)

  }

  def filterField(filter: Property[String], fieldQuery: FieldQuery, filterType:String):Modifier = {

    fieldQuery.field.`type` match {
      case JSONFieldTypes.TIME => DateTimeWidget.Time(Property(""),"",filter.transform(_.asJson,_.string)).render()
      case JSONFieldTypes.DATE => DateTimeWidget.Date(Property(""),"",filter.transform(_.asJson,_.string)).render()
      case JSONFieldTypes.DATETIME => DateTimeWidget.DateTime(Property(""),"",filter.transform(_.asJson,_.string)).render()
      case JSONFieldTypes.NUMBER if fieldQuery.field.lookup.isEmpty && filterType != Filter.BETWEEN => {
        NumberInput.debounced(filter,cls := "form-control")
      }
      case _ => TextInput.debounced(filter,cls := "form-control")
    }

  }

  override def getTemplate: scalatags.generic.Modifier[Element] = {

    val pagination = {

      div(
        Labels.navigation.recordFound,br,bind(model.subProp(_.ids.count))," - ",
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
      h3(GlobalStyles.noMargin,labelTitle),
      div(BootstrapStyles.pullLeft) (
        if (model.get.kind != VIEW.kind)
          a(GlobalStyles.boxButton,Navigate.click(routes.add()))(Labels.entities.`new` + " ",bind(model.subProp(_.name)))
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
                  repeat(model.subSeq(_.fieldQueries)) { fieldQuery =>
                      val title: String = fieldQuery.get.field.label.getOrElse(fieldQuery.get.field.name)
                      val filter = fieldQuery.asModel.subProp(_.filter)
                      val sort = fieldQuery.asModel.subProp(_.sort)

                    th(GlobalStyles.smallCells)(
                      a(
                        onclick :+= ((ev: Event) => presenter.sort(fieldQuery.get), true),
                        title," ",
                        Labels(Sort.label(sort.get))
                      ),br,
                      filterOptions(fieldQuery.asModel),
                      produce(fieldQuery.asModel.subProp(_.filterType)) { ft =>
                        span(filterField(filter, fieldQuery.get, ft)).render
                      }
                    ).render

                }
            ).render
          }),
          rowFactory = (el) => {
            val key = presenter.ids(el.get)

            val hasKey = model.get.metadata.exists(_.keys.nonEmpty)

            val selected = model.subProp(_.selectedRow).transform(_.exists(_ == el.get))

            tr((`class` := "info").attrIf(selected), onclick :+= ((e:Event) => presenter.selected(el.get),true),
              td(GlobalStyles.smallCells)(
                hasKey match{
                  case false => p(color := "grey")(Labels.entity.no_action)
                  case true => Seq(a(
                    cls := "primary",
                    onclick :+= ((ev: Event) => presenter.edit(el.get), true)
                  )(Labels.entity.edit),span(" "),a(
                    cls := "danger",
                    onclick :+= ((ev: Event) => presenter.delete(el.get), true)
                  )(Labels.entity.delete))
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
        a(`type` := "button", onclick :+= ((e:Event) => presenter.downloadCSV()),GlobalStyles.boxButton,"Download CSV"),
        showIf(model.subProp(_.fieldQueries).transform(_.size == 0)){ p("loading...").render },
        br,br
      ),
      Debug(model)
    )
  }


}
