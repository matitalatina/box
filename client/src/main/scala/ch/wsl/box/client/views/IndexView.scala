package ch.wsl.box.client.views

import io.udash._
import ch.wsl.box.client._
import org.scalajs.dom.Element
import ch.wsl.box.client.styles.{GlobalStyles}
import scalacss.ScalatagsCss._

object IndexViewPresenter extends DefaultViewPresenterFactory[IndexState.type](() => new IndexView)

class IndexView extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  private val content = div(
    h2("Postgres Box Client"),
    table(tr(td(GlobalStyles.smallCells)("test")))
  )

  override def getTemplate: Modifier = content

  override def renderChild(view: View): Unit = {}
}