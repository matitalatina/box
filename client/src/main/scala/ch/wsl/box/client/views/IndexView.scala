package ch.wsl.box.client.views

import io.udash._
import ch.wsl.box.client._
import ch.wsl.box.client.services.REST
import org.scalajs.dom.{Element, Event}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.{Session, UI}
import ch.wsl.box.model.shared.{JSONQuery, JSONSort, Sort}
import io.circe.Json

import scalacss.ScalatagsCss._
import Context._
import ch.wsl.box.client.views.components.Debug
import io.udash.bootstrap.BootstrapStyles

case class IndexViewModel(news:Seq[Json])
object IndexViewModel extends HasModelPropertyCreator[IndexViewModel] {
  implicit val blank: Blank[IndexViewModel] =
    Blank.Simple(IndexViewModel(Seq()))
}

object IndexViewPresenter extends ViewPresenter[IndexState.type]{

  val prop = ModelProperty.blank[IndexViewModel]

  override def create() = (new IndexView(prop),new IndexPresenter(prop))
}

class IndexPresenter(viewModel:ModelProperty[IndexViewModel]) extends Presenter[IndexState.type] {
  override def handleState(state: IndexState.type): Unit = {
    for{
      news <- REST.list("entity",Session.lang(),UI.newsTable.getOrElse("news"),JSONQuery(List(), List(JSONSort("news_id",Sort.DESC)),10,1))
    } yield {
      viewModel.set(IndexViewModel(news))
    }
  }
}

class IndexView(viewModel:ModelProperty[IndexViewModel]) extends View {
  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.JsonUtils._
  import io.udash.css.CssView._


  import org.scalajs.dom.File


  private val content = div(BootstrapStyles.row)(
    div(raw(UI.info)),
    if(UI.enableNews) {
      div(h2("News"),
        Debug(viewModel, name = "indexView"),
        repeat(viewModel.subSeq(_.news)) { news =>
          div(
            div(news.get.get("news_id")),
            div(pre(news.get.get(Session.lang()))),
            br
          ).render
        }
      )
    } else div()
  )


  override def getTemplate: Modifier = content

}