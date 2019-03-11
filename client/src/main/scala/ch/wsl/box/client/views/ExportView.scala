package ch.wsl.box.client.views



import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.{EntityFormState, EntityTableState, ExportState}
import ch.wsl.box.client.services.{Enhancer, Navigate, REST}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils._
import ch.wsl.box.client.views.components.widget.Widget
import ch.wsl.box.client.views.components.{Debug, JSONMetadataRenderer, TableFieldsRenderer}
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash.{showIf, _}
import io.udash.bootstrap.{BootstrapStyles, UdashBootstrap}
import io.udash.bootstrap.label.UdashLabel
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

case class ExportModel(metadata:Option[JSONMetadata], queryData:Json, headers:Seq[String],data:Seq[Seq[String]], exportDef:Option[ExportDef])

object ExportModel extends HasModelPropertyCreator[ExportModel]{
  implicit val blank: Blank[ExportModel] =
    Blank.Simple(ExportModel(None, Json.Null, Seq(), Seq(), None))
}

object ExportViewPresenter extends ViewPresenter[ExportState] {

  import ch.wsl.box.client.Context._
  override def create(): (View, Presenter[ExportState]) = {
    val model = ModelProperty.blank[ExportModel]
    val presenter = ExportPresenter(model)
    (ExportView(model,presenter),presenter)
  }
}

case class ExportPresenter(model:ModelProperty[ExportModel]) extends Presenter[ExportState] with Logging {

  import ch.wsl.box.client.Context._
  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JSONUtils._


  override def handleState(state: ExportState): Unit = {
    for{
      metadata <- REST.exportMetadata(state.export,Session.lang())
      exportDef <- REST.exportDef(state.export, Session.lang())
    } yield {
      model.subProp(_.metadata).set(Some(metadata))
      model.subProp(_.queryData).set(Json.obj(JSONMetadata.jsonPlaceholder(metadata,Seq()).toSeq :_*))
      model.subProp(_.exportDef).set(Some(exportDef))
    }
  }

  private def args = {
    val qd = model.get.queryData
    model.get.metadata.get.tabularFields.map(k => qd.js(k))
  }

  def csv() = {
    logger.info()
    val url = s"api/v1/export/${model.get.metadata.get.entity}/${Session.lang()}?q=${args.map(_.injectLang(Session.lang)).asJson.toString()}".replaceAll("\n","")
    logger.info(s"downloading: $url")
    dom.window.open(url)
  }

  def query() = {

    for{
      data <- REST.export(model.get.metadata.get.entity,args,Session.lang())
    } yield {
      model.subProp(_.headers).set(data.headOption.getOrElse(Seq()))
      model.subProp(_.data).set(Try(data.tail).getOrElse(Seq()))
    }
  }
}

case class ExportView(model:ModelProperty[ExportModel], presenter:ExportPresenter) extends View {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._


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
    JSONMetadataRenderer(metadata, model.subProp(_.queryData),Seq()).edit(),
    button(Labels.exports.load,onclick :+= ((e:Event) => presenter.query()),ClientConf.style.boxButton),
    button(Labels.exports.csv,onclick :+= ((e:Event) => presenter.csv()),ClientConf.style.boxButton),
      UdashTable()(model.subSeq(_.data))(
        headerFactory = Some(() => {
          tr(
            repeat(model.subSeq(_.headers)) { header =>
              th(ClientConf.style.smallCells)(bind(header)).render
            }
          ).render
        }),
        rowFactory = (el) => {
          tr(
            produce(el) { cell =>
              cell.map { c =>
                td(ClientConf.style.smallCells)(c).render
              }
            }
          ).render
        }
      ).render,
      br,br
  ).render
}
