package ch.wsl.box.client.views

import io.udash._
import ch.wsl.box.client.IndexState
import ch.wsl.box.client.services.Labels
import io.udash.bootstrap.BootstrapStyles
import org.scalajs.dom.Element

object ErrorViewPresenter extends StaticViewFactory[IndexState.type](() => new ErrorView)

class ErrorView extends View {
  import scalatags.JsDom.all._
  import io.udash.css.CssView._

  private val content = div(BootstrapStyles.Grid.row) (
    h3(
      Labels.error.notfound
    )
  )

  override def getTemplate: Modifier = content

}
