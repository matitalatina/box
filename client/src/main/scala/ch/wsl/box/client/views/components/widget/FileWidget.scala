package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.Conf
import ch.wsl.box.client.views.components.Debug
import ch.wsl.box.model.shared.{JSONField, JSONMetadata}
import io.circe.Json
import io.udash._
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom.File


import scala.concurrent.Future

case class FileWidget(id:Property[String], prop:Property[Json], field:JSONField, labelString:String, entity:String) extends Widget {

  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JsonUtils._
  import io.circe.syntax._

  override def afterSave(result:Json, metadata: JSONMetadata) = {
    println(s"File after save with result: $result with selected file: ${selectedFiles.get.headOption.map(_.name)}")
    val jsonid = result.ID(metadata.keys)
    for{
      _ <- Future.sequence{
        selectedFiles.get.map(REST.sendFile(_,jsonid,s"${metadata.entity}.${field.file.get.file_field}"))
      }
    } yield Unit
  }

  val selectedFiles: SeqProperty[File] = SeqProperty(Seq.empty[File])
  val input = FileInput("file", Property(false), selectedFiles)()

  selectedFiles.listen{ files =>
    prop.set(files.headOption.map(_.name).asJson)
  }


  override def render() = {
    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      "ID:",bind(id),br,
      if(labelString.length > 0) label(labelString) else {},
      input,
      repeat(selectedFiles) { sf =>
        div(
          "debug",
          bind(sf.transform(x => x.name))
        ).render
      },
      h4("Selected files"),
      ul(
        produce(prop.transform(_.string)) { name =>
          div(
          img(src := s"/api/v1/file/${entity}.${field.file.get.file_field}/${id.get}", height := Conf.imageHeight),
//          img(src := REST.getFile(entity, id.get), height := Conf.imageHeight),
//          a(href := REST.getFile(entity, id.get), name)
          a(href := s"/api/v1/file/${entity}.${field.file.get.file_field}/${id.get}",name)
          ).render
        }
      ),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  }


}