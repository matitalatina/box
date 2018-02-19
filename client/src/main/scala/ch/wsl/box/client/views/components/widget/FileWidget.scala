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


/**
  *
  * @param id
  * @param prop          holds the filename
  * @param field
  * @param labelString
  * @param entity
  */
case class FileWidget(id:Property[String], prop:Property[Json], field:JSONField, labelString:String, entity:String) extends Widget {

  import scalatags.JsDom.all._
  import scalacss.ScalatagsCss._
  import ch.wsl.box.client.Context._
  import ch.wsl.box.shared.utils.JsonUtils._
  import io.circe.syntax._

  override def afterSave(result:Json, metadata: JSONMetadata) = {
    println(s"File after save with result: $result with selected file: ${selectedFile.get.headOption.map(_.name)}")

    val jsonid = result.ID(metadata.keys)
    for{
      idfile <- Future.sequence{
        val r: Seq[Future[Int]] = selectedFile.get.map(REST.sendFile(_,jsonid,s"${metadata.entity}.${field.file.get.file_field}"))
        r
      }
    } yield {
      idfile.headOption.map(x => id.set(x.toString()))
    }
  }

  val selectedFile: SeqProperty[File] = SeqProperty(Seq.empty[File])
  val input = FileInput("file", Property(false), selectedFile)()

  selectedFile.listen{ files =>
    prop.set(files.headOption.map(_.name).asJson)
  }


  override def render() = {
    div(BootstrapCol.md(12),GlobalStyles.noPadding,
      "ID:",bind(id),br,
      if(labelString.length > 0) label(labelString) else {},
      input,
      repeat(selectedFile) { sf =>
        div(
          "debug",
          bind(sf.transform(x => x.name))
        ).render
      },
      h4("Selected files"),
      ul(
        produce(prop.transform(_.string)) { name =>
          div(
            produce(id) { idfile =>
              div(
                img(src := s"/api/v1/file/${entity}.${field.file.get.file_field}/${idfile}", height := Conf.imageHeight),
                //          img(src := REST.getFile(entity, id.get), height := Conf.imageHeight),
                //          a(href := REST.getFile(entity, id.get), name)
                a(href := s"/api/v1/file/${entity}.${field.file.get.file_field}/${idfile}", name)
              ).render
            }
          ).render
        }
      ),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  }


}