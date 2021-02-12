package ch.wsl.box.client.views.components.widget


import java.util.UUID

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.{ClientConf, REST}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.views.components.Debug
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bindings.Bindings
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLAnchorElement
import org.scalajs.dom.{Event, File, FileReader}
import scalatags.JsDom
import scribe.Logging

import scala.concurrent.Future
import scala.scalajs.js
import scala.util.Random


/**
  *
  * @param id
  * @param prop          holds the filename
  * @param field
  * @param entity
  */
case class FileSimpleWidget(id:Property[Option[String]], prop:Property[Json], field:JSONField, entity:String) extends Widget with Logging {

  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import io.udash.css.CssView._
  import ch.wsl.box.shared.utils.JSONUtils._
  import io.circe.syntax._


  private def downloadFile(): Unit = {
    val file = prop.get.string
    val mime = file.take(1) match {
      case "/" => "image/jpeg"
      case "i" => "image/png"
      case "R" => "image/gif"
      case "J" => "application/pdf"
      case _ => "application/octet-stream"
    }
    val source = s"data:$mime;base64,$file"
    val link = dom.document.createElement("a").asInstanceOf[HTMLAnchorElement]
    val extension = mime.split("/")(1) match {
      case "octet-strem" => "bin"
      case s:String => s
    }
    val filename = s"download.$extension"
    link.href = source
    link.asInstanceOf[js.Dynamic].download = filename
    link.click()
  }

  private def download = {

    div(BootstrapCol.md(12),ClientConf.style.noPadding)(
      WidgetUtils.toLabel(field),
      button("Download",ClientConf.style.boxButton,BootstrapStyles.Float.right(), onclick :+= ((e:Event) => downloadFile()) ),
      div(BootstrapStyles.Visibility.clearfix)
    )
  }



  val acceptMultipleFiles = Property(false)
  val selectedFiles = SeqProperty.blank[File]
  val fileInput = FileInput(selectedFiles, acceptMultipleFiles)("files",display.none).render

  selectedFiles.listen{ _.headOption.map{ file =>
    val reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      val result = reader.result.asInstanceOf[String]
      val token = "base64,"
      val index = result.indexOf(token)
      val base64 = result.substring(index+token.length)
      prop.set(base64.asJson)
    }
  }}

  private def upload = {

    div(BootstrapCol.md(12),ClientConf.style.noPadding)(
      button("Upload",ClientConf.style.boxButton,BootstrapStyles.Float.right(), onclick :+= ((e:Event) => fileInput.click()) ),
      div(BootstrapStyles.Visibility.clearfix)
    )
  }

  override protected def show(): JsDom.all.Modifier = div(BootstrapCol.md(12),ClientConf.style.noPadding,
    download,
    div(BootstrapStyles.Visibility.clearfix),
  ).render

  override def edit() = {
    div(BootstrapCol.md(12),ClientConf.style.noPadding,
      download,
      upload,
      //autoRelease(produce(id) { _ => div(FileInput(selectedFile, Property(false))("file")).render }),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  }


}

object FileSimpleWidgetFactory extends ComponentWidgetFactory {

  override def name: String = WidgetsNames.simpleFile

  override def create(params: WidgetParams): Widget = FileSimpleWidget(params.id,params.prop,params.field,params.metadata.entity)

}
