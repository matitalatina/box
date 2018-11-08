package ch.wsl.box.client.views

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{EntityFormState, EntityTableState}
import ch.wsl.box.client.services.{Enhancer, Navigate, Notification, REST}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.{ClientConf, Labels, Navigation, Session}
import ch.wsl.box.client.views.components.widget.{DateTimeWidget, SelectWidget, SelectWidgetFullWidth}
import ch.wsl.box.client.views.components.{Debug, TableFieldsRenderer}
import ch.wsl.box.model.shared.EntityKind.VIEW
import ch.wsl.box.model.shared._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.udash._
import io.udash.bootstrap.{BootstrapStyles, UdashBootstrap}
import io.udash.bootstrap.label.UdashLabel
import io.udash.bootstrap.table.UdashTable
import io.udash.properties.single.Property
import org.scalajs.dom
import scalacss.ScalatagsCss._
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{Element, Event, KeyboardEvent, window}
import scalacss.internal.Pseudo.Lang
import scalatags.JsDom.all.a
import scribe.Logging

import scala.concurrent.Future
import scala.util.Try

case class IDsVM(isLastPage:Boolean,
                    currentPage:Int,
                    ids:Seq[String],
                    count:Int    //stores the number of rows resulting from the query without paging
                   )

case class Row(data: Seq[String])

case class FieldQuery(field:JSONField, sort:String, filterValue:String, filterOperator:String)

case class EntityTableModel(name:String, kind:String, rows:Seq[Row], fieldQueries:Seq[FieldQuery],
                            metadata:Option[JSONMetadata], selectedRow:Option[Row], ids: IDsVM, pages:Int, write:Boolean)


object EntityTableModel extends HasModelPropertyCreator[EntityTableModel]{
  def empty = EntityTableModel("","",Seq(),Seq(),None,None,IDsVM(true,1,Seq(),0),1, false)
  implicit val blank: Blank[EntityTableModel] =
    Blank.Simple(empty)
}

object FieldQuery extends HasModelPropertyCreator[FieldQuery]
object IDsVM extends HasModelPropertyCreator[IDsVM] {
  def fromIDs(ids:IDs) = IDsVM(
    ids.isLastPage,
    ids.currentPage,
    ids.ids,
    ids.count
  )
}


case class EntityTableViewPresenter(routes:Routes, onSelect:Seq[(JSONField,String)] => Unit = (f => Unit)) extends ViewFactory[EntityTableState] {

  import ch.wsl.box.client.Context._



  override def create(): (View, Presenter[EntityTableState]) = {

    val model = ModelProperty.blank[EntityTableModel]

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


  private var filterUpdateHandler: Int = 0
//  final private val SKIP_RELOAD_ROWS:Int = 999

  model.subProp(_.fieldQueries).listen{ fq =>

    logger.info("filterUpdateHandler " + filterUpdateHandler)

    if (filterUpdateHandler != 0) window.clearTimeout(filterUpdateHandler)

    filterUpdateHandler = window.setTimeout(() => {
      reloadRows(1)
    }, 500)

  }



  override def handleState(state: EntityTableState): Unit = {

    logger.info(s"handling Entity table state name=${state.entity} and kind=${state.kind}")

    model.set(EntityTableModel.empty)
    model.subProp(_.name).set(state.entity)
    model.subProp(_.kind).set(state.kind)

    val emptyJsonQuery = JSONQuery.empty.limit(ClientConf.pageLength)


    {for{
      emptyFieldsForm <- REST.metadata(state.kind,Session.lang(),state.entity)
      fields = emptyFieldsForm.fields.filter(field => emptyFieldsForm.tabularFields.contains(field.name))
      form = emptyFieldsForm.copy(fields = fields)

      defaultQuery:JSONQuery = form.query match {
        case None => emptyJsonQuery
        case Some(jsonquery) => jsonquery.copy(paging = emptyJsonQuery.paging)   //in case a specific sorting or filtering is specified in box.form
      }

      query:JSONQuery = Session.getQuery() match {
        case None => defaultQuery
        case Some(jsonquery) => jsonquery      //in case a query is already stored in Session
      }

      qEncoded = encodeFk(fields,query)

      access <- REST.writeAccess(form.baseTable)
      csv <- REST.csv(state.kind, Session.lang(), state.entity, qEncoded)
      ids <- REST.ids(state.kind, Session.lang(), state.entity, qEncoded)
//      ids <- REST.ids(model.get.kind,Session.lang(),model.get.name,query)
//      all_ids <- REST.ids(model.get.kind,Session.lang(),model.get.name, JSONQuery.empty.limit(100000))
      specificKind <- REST.specificKind(state.kind, Session.lang(), state.entity)
    } yield {


      val m = EntityTableModel(
        name = state.entity,
        kind = specificKind,
        rows = csv.map(Row(_)),
        fieldQueries = form.tabularFields.flatMap(x => form.fields.find(_.name == x)).map{ field =>

          val operator = query.filter.find(_.column == field.name).flatMap(_.operator).getOrElse(Filter.default(field))
          val rawValue = query.filter.find(_.column == field.name).map(_.value).getOrElse("")
          FieldQuery(
            field = field,
            sort = query.sort.find(_.column == field.name).map(_.order).getOrElse(Sort.IGNORE),
            filterValue = rawValue,
            filterOperator = operator
          )
        },
        metadata = Some(form),
        selectedRow = None,
        ids = IDsVM.fromIDs(ids),
        pages = Navigation.pageCount(ids.count),
        write = access
      )

      saveIds(ids,query)

      model.set(m)
      model.subProp(_.name).set(state.entity)  //it is not set by the above line

    }}.recover{ case e => e.printStackTrace() }
  }


  def ids(el:Row): JSONID = Enhancer.extractID(el.data,model.subProp(_.metadata).get.toSeq.flatMap(_.tabularFields),model.subProp(_.metadata).get.toSeq.flatMap(_.keys))

  def edit(el:Row) = {
    val k = ids(el)
    val newState = routes.edit(k.asString)
    Navigate.to(newState)
  }

  def show(el:Row) = {
    val k = ids(el)
    val newState = routes.show(k.asString)
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

  private def encodeFk(fields:Seq[JSONField],query:JSONQuery):JSONQuery = {

    def getFieldLookup(name:String) = fields.find(_.name == name).toSeq.flatMap(_.lookup).flatMap(_.lookup)

    val filters = query.filter.map{ field =>
      field.operator match {
        case Some(Filter.FK_LIKE) => {
          val ids = getFieldLookup(field.column)
            .filter(_.value.toLowerCase.contains(field.value.toLowerCase()))
            .map(_.id)
          JSONQueryFilter(field.column,Some(Filter.IN),ids.mkString(","))
        }
        case Some(Filter.FK_DISLIKE) => {
          val ids = getFieldLookup(field.column)
            .filter(_.value.toLowerCase.contains(field.value.toLowerCase()))
            .map(_.id)
          JSONQueryFilter(field.column,Some(Filter.NOTIN),ids.mkString(","))
        }
        case Some(Filter.FK_EQUALS) => {
          val id = getFieldLookup(field.column)
            .find(_.value == field.value)
            .map(_.id)
          JSONQueryFilter(field.column,Some(Filter.IN),id.getOrElse(""))  //fails with EQUALS when id = ""
        }
        case Some(Filter.FK_NOT) => {
          val id = getFieldLookup(field.column)
            .find(_.value == field.value)
            .map(_.id)
          JSONQueryFilter(field.column,Some(Filter.NOTIN),id.getOrElse("")) //fails with NOT when id = ""
        }
        case _ => field
      }
    }

    query.copy(filter = filters)

  }

  private def query():JSONQuery = {
    val fieldQueries = model.subProp(_.fieldQueries).get

    val sort = fieldQueries.filter(_.sort != Sort.IGNORE).map(s => JSONSort(s.field.name, s.sort)).toList

    val filter = fieldQueries.filter(_.filterValue != "").map{ f =>
      JSONQueryFilter(f.field.name,Some(f.filterOperator),f.filterValue)
    }.toList

    JSONQuery(filter, sort, None)
  }

  def reloadRows(page:Int): Future[Unit] = {
    logger.info("reloading rows")
    logger.info("filterUpdateHandler "+filterUpdateHandler)

    val q = query().copy(paging = Some(JSONQueryPaging(ClientConf.pageLength, page)))
    val qEncoded = encodeFk(model.get.metadata.toSeq.flatMap(_.fields),q)

    for {
      csv <- REST.csv(model.subProp(_.kind).get, Session.lang(), model.subProp(_.name).get, qEncoded)
      ids <- REST.ids(model.get.kind, Session.lang(), model.get.name, qEncoded)
    } yield {
      model.subProp(_.rows).set(csv.map(Row(_)))
      model.subProp(_.ids).set(IDsVM.fromIDs(ids))
      model.subProp(_.pages).set(Navigation.pageCount(ids.count))
      saveIds(ids, q)
    }

  }

  def filterById(id:JSONID) = {
    val newFieldQueries = model.subProp(_.fieldQueries).get.map{ m =>
      id.id.headOption.exists(_.key == m.field.name) match {
        case true => m.copy(filterValue = id.id.head.value, filterOperator = Filter.EQUALS)
        case false => m
      }
    }
    model.subProp(_.fieldQueries).set(newFieldQueries)
//    reloadRows(1)
  }


  def filter(fieldQuery: FieldQuery, filterValue:String) = {
    logger.info("filtering")
    val newFieldQueries = model.subProp(_.fieldQueries).get.map{ m =>
      m.field.name == fieldQuery.field.name match {
        case true => m.copy(filterValue = filterValue)
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

    val kind = EntityKind(model.subProp(_.kind).get).entityOrForm
    val modelName =  model.subProp(_.name).get
    val exportFields = model.get.metadata.map(_.exportFields).getOrElse(Seq())
    val fields = model.get.metadata.map(_.fields).getOrElse(Seq())

    val queryWithFK = encodeFk(fields,query())


    val url = s"api/v1/$kind/${Session.lang()}/$modelName/csv?fk=${ExportMode.RESOLVE_FK}&fields=${exportFields.mkString(",")}&q=${queryWithFK.asJson.toString()}".replaceAll("\n","")
    logger.info(s"downloading: $url")
    dom.window.open(url)
  }
}

case class EntityTableView(model:ModelProperty[EntityTableModel], presenter:EntityTablePresenter, routes:Routes) extends View with Logging {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._
  import ch.wsl.box.shared.utils.JsonUtils._

  import Enhancer._



  def labelTitle = produce(model.subProp(_.metadata)) { m =>
    val name = m.map(_.label).getOrElse(model.get.name)
    span(name).render
  }

  def filterOptions(fieldQuery: ModelProperty[FieldQuery]) = {


    def label = (id:String) => id match {
      case Filter.FK_NOT => StringFrag("not")
      case Filter.FK_EQUALS => StringFrag("=")
      case Filter.FK_LIKE => StringFrag("contains")
      case Filter.FK_DISLIKE => StringFrag("without")
      case Filter.LIKE => StringFrag("contains")
      case Filter.DISLIKE => StringFrag("without")
      case _ => StringFrag(id)
    }

    Select(fieldQuery.subProp(_.filterOperator), Filter.options(fieldQuery.get.field),label)(GlobalStyles.fullWidth)

  }

  def filterField(filterValue: Property[String], fieldQuery: FieldQuery, filterOperator:String):Modifier = {

    fieldQuery.field.`type` match {
      case JSONFieldTypes.TIME => DateTimeWidget.TimeFullWidth(Property(""),JSONField.empty,filterValue.transform(_.asJson,_.string)).edit()
      case JSONFieldTypes.DATE => DateTimeWidget.DateFullWidth(Property(""),JSONField.empty,filterValue.transform(_.asJson,_.string)).edit()
      case JSONFieldTypes.DATETIME => ClientConf.filterEqualityPrecisionDatetime match{
        case JSONFieldTypes.DATE => DateTimeWidget.DateFullWidth(Property(""),JSONField.empty,filterValue.transform(_.asJson,_.string)).edit()
        case _ => DateTimeWidget.DateTimeFullWidth(Property(""),JSONField.empty,filterValue.transform(_.asJson,_.string)).edit()
      }
      case JSONFieldTypes.NUMBER if fieldQuery.field.lookup.isEmpty && !Seq(Filter.BETWEEN, Filter.IN, Filter.NOTIN).contains(filterOperator) => {
        if(Try(filterValue.get.toDouble).toOption.isEmpty) filterValue.set("")
//        TextInput.debounced(filterValue,cls := "form-control")      //to allow comma separated values, ranges, ...
        NumberInput.debounced(filterValue,cls := "form-control")
      }
//      case JSONFieldTypes.BOOLEAN => {
//        Select(filterValue, cls := "form-control")
//      }
      case _ => TextInput.debounced(filterValue,cls := "form-control")
    }

  }

  override def getTemplate: scalatags.generic.Modifier[Element] = {

    val pagination = {

      div(GlobalStyles.boxNavigationLabel,
        Navigation.button(model.subProp(_.ids.currentPage).transform(_ != 1),() => presenter.reloadRows(1),Labels.navigation.first,_.pullLeft),
        Navigation.button(model.subProp(_.ids.currentPage).transform(_ != 1),() => presenter.reloadRows(model.subProp(_.ids.currentPage).get -1),Labels.navigation.previous,_.pullLeft),
        span(
          " " + Labels.navigation.page + " ",
          bind(model.subProp(_.ids.currentPage)),
          " " + Labels.navigation.of + " ",
          bind(model.subProp(_.pages)),
          " "
        ),
        Navigation.button(model.subModel(_.ids).subProp(_.isLastPage).transform(!_),() => presenter.reloadRows(model.subProp(_.pages).get),Labels.navigation.last,_.pullRight),
        Navigation.button(model.subModel(_.ids).subProp(_.isLastPage).transform(!_),() => presenter.reloadRows(model.subProp(_.ids.currentPage).get + 1),Labels.navigation.next,_.pullRight),
        div(Labels.navigation.recordFound," ",bind(model.subProp(_.ids.count)))
      )
    }


    div(
      div(BootstrapStyles.pullLeft,
        h3(GlobalStyles.noMargin,labelTitle)
      ),
      div(BootstrapStyles.pullRight,GlobalStyles.navigatorArea,
        pagination.render
      ),
      div(BootstrapStyles.Visibility.clearfix),
      produceWithNested(model.subProp(_.write)) { (w,realeser) =>
        if(!w) Seq() else
          div(BootstrapStyles.pullLeft)(
            realeser(produce(model.subProp(_.name)) { m =>
              div(
                button(GlobalStyles.boxButtonImportant, Navigate.click(Routes(model.subProp(_.kind).get, m).add()))(Labels.entities.`new`)
              ).render
            })
          ).render
      },
      div(BootstrapStyles.Visibility.clearfix),
      hr(GlobalStyles.hrThin),
      div(id := "box-table", GlobalStyles.fullHeightMax,
        UdashTable()(model.subSeq(_.rows))(

          headerFactory = Some(() => {
              tr(
                th(GlobalStyles.smallCells)(Labels.entity.actions),
                  repeat(model.subSeq(_.fieldQueries)) { fieldQuery =>
                      val title: String = fieldQuery.get.field.label.getOrElse(fieldQuery.get.field.name)
                      val filterValue = fieldQuery.asModel.subProp(_.filterValue)
                      val sort = fieldQuery.asModel.subProp(_.sort)

                    th(GlobalStyles.smallCells)(
                      a(
                        onclick :+= ((ev: Event) => presenter.sort(fieldQuery.get), true),
                        title," ",
                        Labels(Sort.label(sort.get))
                      ),br,
                      filterOptions(fieldQuery.asModel),
                      produce(fieldQuery.asModel.subProp(_.filterOperator)) { ft =>
                        span(filterField(filterValue, fieldQuery.get, ft)).render
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
                (hasKey,model.get.write) match{
                  case (false,_) => p(color := "grey")(Labels.entity.no_action)
                  case (true,false) => a(
                    cls := "primary",
                    onclick :+= ((ev: Event) => presenter.show(el.get), true)
                  )(Labels.entity.show)
                  case (true,true) => Seq(a(
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

        button(`type` := "button", onclick :+= ((e:Event) => presenter.downloadCSV()),GlobalStyles.boxButton,"Download CSV"),
        showIf(model.subProp(_.fieldQueries).transform(_.size == 0)){ p("loading...").render },
        br,br
      ),
      Debug(model)
    )
  }


}
