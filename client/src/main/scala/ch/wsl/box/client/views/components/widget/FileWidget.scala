package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.ClientConf
import ch.wsl.box.client.views.components.Debug
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash._
import io.udash.bindings.Bindings
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom.File
import scalatags.JsDom
import scribe.Logging

import scala.concurrent.Future


/**
  *
  * @param id
  * @param prop          holds the filename
  * @param field
  * @param labelString
  * @param entity
  */
case class FileWidget(id:Property[String], prop:Property[Json], field:JSONField, entity:String) extends Widget with Logging {

  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import io.udash.css.CssView._
  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JSONUtils._
  import io.circe.syntax._


  override def afterSave(result:Json, metadata: JSONMetadata) = {
    logger.info(s"File after save with result: $result with selected file: ${selectedFile.get.headOption.map(_.name)}")

    val jsonid = result.ID(metadata.keys)
    logger.info(s"jsonid = $jsonid")
    for{
      idfile <- Future.sequence{
        val r: Seq[Future[Int]] = selectedFile.get.map(REST.sendFile(_,jsonid,s"${metadata.entity}.${field.file.get.file_field}"))
        r
      }
    } yield {
      logger.info("image saved")
      //idfile.headOption.map(x => id.set(x.toString()))
      id.set(jsonid.asString)
    }
  }

  val selectedFile: SeqProperty[File] = SeqProperty(Seq.empty[File])
  val input = FileInput("file", Property(false), selectedFile)()

  selectedFile.listen{ files =>
    prop.set(files.headOption.map(_.name).asJson)
  }


  override protected def show(): JsDom.all.Modifier = div(BootstrapCol.md(12),GlobalStyles.noPadding,
    label(field.title),
    produceWithNested(prop.transform(_.string)) { (name,nested) =>
      div(
        nested(produce(id) { idfile =>
          logger.info("rendering image")
          val randomString = UUID.randomUUID().toString
          JSONID.fromString(idfile) match {
            case Some(_) => div(
              img(src := s"api/v1/file/${entity}.${field.file.get.file_field}/${idfile}/thumb?$randomString",GlobalStyles.imageThumb) ,br,
              a(href := s"api/v1/file/${entity}.${field.file.get.file_field}/${idfile}", name)
            ).render
            case None => div().render
          }

        })
      ).render
    },
    div(BootstrapStyles.Visibility.clearfix)
  ).render

  override def edit() = {

    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      WidgetUtils.toLabel(field),
      produceWithNested(prop) { (name,nested) =>
        div(
          nested(produce(id) { idfile =>
            logger.info("rendering image")
            val randomString = UUID.randomUUID().toString
            JSONID.fromString(idfile) match {
              case Some(_) => div(
                img(src := s"api/v1/file/${entity}.${field.file.get.file_field}/${idfile}/thumb?$randomString",GlobalStyles.imageThumb) ,br,
                a(href := s"api/v1/file/${entity}.${field.file.get.file_field}/${idfile}", name.string)
              ).render
              case None => div().render
            }

          })
        ).render
      },
      input,
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  }


}

case class FileWidgetFactory( entity:String) extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = FileWidget(id,prop,field,entity)
}