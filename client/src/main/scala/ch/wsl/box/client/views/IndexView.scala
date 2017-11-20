package ch.wsl.box.client.views

import io.udash._
import ch.wsl.box.client._
import ch.wsl.box.client.services.REST
import org.scalajs.dom.{Element, Event}
import ch.wsl.box.client.styles.GlobalStyles

import scalacss.ScalatagsCss._

object IndexViewPresenter extends DefaultViewPresenterFactory[IndexState.type](() => new IndexView)

class IndexView extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  import org.scalajs.dom.File

  val selectedFiles: SeqProperty[File] = SeqProperty(Seq.empty)


  val input = FileInput("file", Property(false), selectedFiles)()


  private val content = div(
    h2("Postgres Box Client"),
    input,
    h4("Selected files"),
    ul(
      repeat(selectedFiles)(file => {
        li(file.get.name).render
      })
    ),
    button(onclick :+= ((e:Event) => selectedFiles.get.foreach(REST.sendFile)),"Send")
  )


  override def getTemplate: Modifier = content

  override def renderChild(view: View): Unit = {}
}