package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.ClientConf
import ch.wsl.box.client.views.components.Debug
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bindings.Bindings
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom
import org.scalajs.dom.File
import scalatags.JsDom
import scribe.Logging

import scala.concurrent.Future
import scala.util.Random


object FileWidget{
  private var files:collection.mutable.Map[String,dom.File] = collection.mutable.Map()
}

/**
  *
  * @param id
  * @param prop          holds the filename
  * @param field
  * @param entity
  */
case class FileWidget(id:Property[String], prop:Property[Json], field:JSONField, entity:String) extends Widget with Logging {

  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import io.udash.css.CssView._
  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JSONUtils._
  import io.circe.syntax._

  val urlProp:ReadableProperty[Option[String]] = id.transform{ idString =>
    JSONID.fromString(idString).map{ id =>
      s"/file/${entity}.${field.file.get.file_field}/${idString}"
    }
  }

  logger.info("Creating new FileWidget")


  id.listen(_ => FileWidget.files.clear())

  override def afterSave(result:Json, metadata: JSONMetadata) = {
    logger.info(s"File after save with result: $result with selected file: ${selectedFile.get.headOption.map(_.name)}, prop: ${prop.get}")
    val jsonid = result.ID(metadata.keys)
    logger.info(s"jsonid = $jsonid")
    for{
      idfile <- Future.sequence{
        val r: Seq[Future[Int]] = selectedFile.get.map(REST.sendFile(_,jsonid,s"${metadata.entity}.${field.file.get.file_field}"))
        r
      }
    } yield {
      logger.info("image saved")
      id.set(jsonid.asString,true)
    }
  }


  val selectedFile: SeqProperty[File] = SeqProperty(Seq.empty[File])
  val input = FileInput(selectedFile, Property(false))("file")

  selectedFile.listen{ files =>
    logger.info(s"selected file changed ${files.map(_.name)}")
    prop.set(files.headOption.map(_.name).asJson)
  }


  private def showImage = {
    logger.debug("showImage")
    produceWithNested(urlProp) { (url,nested) =>
      logger.info("rendering image")
      val randomString = UUID.randomUUID().toString
      url match {
        case Some(u) => div(
          img(src := Routes.apiV1(s"${u}/thumb?$randomString"),ClientConf.style.imageThumb) ,br,
          nested(produce(prop) { name => a(href := Routes.apiV1(u), name.string).render })
        ).render
        case None => div().render
      }
    }
  }

  override protected def show(): JsDom.all.Modifier = div(BootstrapCol.md(12),ClientConf.style.noPadding,
    label(field.title),
    showImage,
    div(BootstrapStyles.Visibility.clearfix)
  ).render

  override def edit() = {



    div(BootstrapCol.md(12),ClientConf.style.noPadding,
      WidgetUtils.toLabel(field),
      showImage,
      input,
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  }


}

case class FileWidgetFactory( entity:String) extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = FileWidget(id,prop,field,entity)
}
