package ch.wsl.box.client.views



import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{DataKind, DataState, EntityFormState, EntityTableState}
import ch.wsl.box.client.services.{ClientConf, Labels, Navigate, REST}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils._
import ch.wsl.box.client.views.components.widget.Widget
import ch.wsl.box.client.views.components.{Debug, JSONMetadataRenderer, TableFieldsRenderer}
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash.{showIf, _}
import io.udash.bootstrap.{BootstrapStyles, UdashBootstrap}
import io.udash.bootstrap.table.UdashTable
import io.udash.core.Presenter
import io.udash.properties.single.Property
import org.scalajs.dom
import org.scalajs.dom._
import scribe.Logging

import scala.concurrent.Future
import scalatags.JsDom
import scalacss.ScalatagsCss._
import scalatags.generic.Modifier
import scalatags.generic

import scala.util.Try


/**
  * Created by andre on 4/24/2017.
  */

case class DataModel(metadata:Option[JSONMetadata], queryData:Json, headers:Seq[String], data:Seq[Seq[String]], exportDef:Option[ExportDef], kind:String)

object DataModel extends HasModelPropertyCreator[DataModel]{
  implicit val blank: Blank[DataModel] =
    Blank.Simple(DataModel(None, Json.Null, Seq(), Seq(), None,""))
}

object DataViewPresenter extends ViewFactory[DataState] {

  override def create(): (View, Presenter[DataState]) = {
    val model = ModelProperty.blank[DataModel]
    val presenter = DataPresenter(model)
    (DataView(model,presenter),presenter)
  }
}

case class DataPresenter(model:ModelProperty[DataModel]) extends Presenter[DataState] with Logging {

  import ch.wsl.box.client.Context._

  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JSONUtils._


  override def handleState(state: DataState): Unit = {
    model.subProp(_.kind).set(state.kind)

    for{
      metadata <- services.rest.dataMetadata(state.kind,state.export,services.clientSession.lang())
      exportDef <- services.rest.dataDef(state.kind,state.export, services.clientSession.lang())
    } yield {
      model.subProp(_.metadata).set(Some(metadata))
      model.subProp(_.queryData).set(Json.obj(JSONMetadata.jsonPlaceholder(metadata,Seq()).toSeq :_*))
      model.subProp(_.exportDef).set(Some(exportDef))
    }
  }

  private def args = {
    if(model.get.kind == DataKind.EXPORT) {
      val qd = model.get.queryData
      model.get.metadata.get.tabularFields.map(k => qd.js(k)).map(_.injectLang(services.clientSession.lang())).asJson
    } else {
      model.get.queryData
    }
  }

  def csv() = {
    logger.info()
    val url = Routes.apiV1(
      s"/${model.get.kind}/${model.get.metadata.get.name}/${services.clientSession.lang()}?q=${args.toString()}".replaceAll("\n","")
    )
    logger.info(s"downloading: $url")
    dom.window.open(url)
  }

  def query() = {

    for{
      data <- services.rest.data(model.get.kind,model.get.metadata.get.name,args,services.clientSession.lang())
    } yield {
      model.subProp(_.headers).set(data.headOption.getOrElse(Seq()))
      model.subProp(_.data).set(Try(data.tail).getOrElse(Seq()))
    }
  }
}

case class DataView(model:ModelProperty[DataModel], presenter:DataPresenter) extends View {

  import scalatags.JsDom.all._
  import io.udash.css.CssView._
  import ch.wsl.box.shared.utils.JSONUtils._

  def table = model.transform(_.exportDef.exists(_.mode == FunctionKind.Modes.TABLE))
  def pdf = model.transform(_.exportDef.exists(_.mode == FunctionKind.Modes.PDF))
  def html = model.transform(_.exportDef.exists(_.mode == FunctionKind.Modes.HTML))

  override def getTemplate = div(
    produce(model.subProp(_.metadata)) {
      case None => div("loading").render
      case Some(metadata) => loaded(metadata)
    }
  )

  def loaded(metadata: JSONMetadata) = div(
    br,br,
    h3(ClientConf.style.noMargin,
      metadata.label
    ),
    produce(model.subProp(_.exportDef)){ ed =>
      div(ClientConf.style.global)(ed.map(_.description.getOrElse[String]("")).getOrElse[String]("")).render
    },
    br,
    JSONMetadataRenderer(metadata, model.subProp(_.queryData),Seq(),Property(model.get.queryData.ID(metadata.keys).map(_.asString))).edit(),
    showIf(table) { button(Labels.exports.load,onclick :+= ((e:Event) => presenter.query()),ClientConf.style.boxButton).render },
    showIf(table) { button(Labels.exports.csv,onclick :+= ((e:Event) => presenter.csv()),ClientConf.style.boxButton).render },
    showIf(pdf) { button(Labels.exports.pdf,onclick :+= ((e:Event) => presenter.csv()),ClientConf.style.boxButton).render },
    showIf(html) { button(Labels.exports.html,onclick :+= ((e:Event) => presenter.csv()),ClientConf.style.boxButton).render },
      UdashTable(model.subSeq(_.data))(
        headerFactory = Some(_ => {
          tr(
            repeat(model.subSeq(_.headers)) { header =>
              td(ClientConf.style.smallCells)(bind(header)).render
            }
          ).render
        }),
        rowFactory = (el,nested) => {
          tr(
            nested(produce(el) { cell =>
              cell.map { c =>
                td(ClientConf.style.smallCells)(c).render
              }
            })
          ).render
        }
      ).render,
      br,br
  ).render
}
