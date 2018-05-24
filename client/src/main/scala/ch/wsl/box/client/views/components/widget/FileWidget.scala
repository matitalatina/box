package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.Conf
import ch.wsl.box.client.views.components.Debug
import ch.wsl.box.model.shared.{JSONField, JSONID, JSONMetadata}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom.File
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
case class FileWidget(id:Property[String], prop:Property[Json], field:JSONField, labelString:String, entity:String) extends Widget with Logging {

  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JsonUtils._
  import io.circe.syntax._

  override def afterSave(result:Json, metadata: JSONMetadata) = {
    logger.info(s"File after save with result: $result with selected file: ${selectedFile.get.headOption.map(_.name)}")

    val jsonid = result.ID(metadata.keys)
    for{
      idfile <- Future.sequence{
        val r: Seq[Future[Int]] = selectedFile.get.map(REST.sendFile(_,jsonid,s"${metadata.entity}.${field.file.get.file_field}"))
        r
      }
    } yield {
      id.set(jsonid.asString)
    }
  }

  val selectedFile: SeqProperty[File] = SeqProperty(Seq.empty[File])
  val input = FileInput("file", Property(false), selectedFile)()

  selectedFile.listen{ files =>
    prop.set(files.headOption.map(_.name).asJson)
  }


  override def render() = {

    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      if(labelString.length > 0) label(labelString) else {},
      produce(prop.transform(_.string)) { name =>
        div(
          produce(id) { idfile =>
            JSONID.fromString(idfile) match {
              case Some(_) => div(
                  img(src := s"/api/v1/file/${entity}.${field.file.get.file_field}/${idfile}/thumb",GlobalStyles.imageThumb) ,br,
                  a(href := s"/api/v1/file/${entity}.${field.file.get.file_field}/${idfile}", name)
                ).render
              case None => div().render
            }

          }
        ).render
      },
      input,
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  }


}