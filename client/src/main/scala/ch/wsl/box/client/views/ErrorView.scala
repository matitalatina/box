package ch.wsl.box.client.views

import io.udash._
import ch.wsl.box.client.IndexState
import ch.wsl.box.client.utils.Labels
import org.scalajs.dom.Element

object ErrorViewPresenter extends DefaultViewPresenterFactory[IndexState.type](() => new ErrorView)

class ErrorView extends View {
  import scalatags.JsDom.all._

  private val content = h3(
    Labels.error.notfound
  )

  override def getTemplate: Modifier = content

  override def renderChild(view: View): Unit = {}
}