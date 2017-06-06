package ch.wsl.box.client.views

import io.udash._
import ch.wsl.box.client._
import org.scalajs.dom.{Element, Event}
import ch.wsl.box.client.styles.GlobalStyles

import scalacss.ScalatagsCss._

object IndexViewPresenter extends DefaultViewPresenterFactory[IndexState.type](() => new IndexView)

class IndexView extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  import org.scalajs.dom.File
  val acceptMultipleFiles: Property[Boolean] = Property(false)
  val selectedFiles: SeqProperty[File] = SeqProperty(Seq.empty)

  val fileUploader = new FileUploader(new Url("/test"))

  val input = FileInput("file", acceptMultipleFiles, selectedFiles)()


  private val content = div(
    h2("Postgres Box Client"),
    input,
    h4("Selected files"),
    ul(
      repeat(selectedFiles)(file => {
        li(file.get.name).render
      })
    ),
    button(onclick :+= ((e:Event) => fileUploader.upload(input)),"Send")
  )


  override def getTemplate: Modifier = content

  override def renderChild(view: View): Unit = {}
}