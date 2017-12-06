package ch.wsl.box.client.views

import io.udash._
import ch.wsl.box.client._
import ch.wsl.box.client.services.REST
import org.scalajs.dom.{Element, Event}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.Session
import ch.wsl.box.model.shared.{JSONQuery, JSONSort, Sort}
import io.circe.Json

import scalacss.ScalatagsCss._
import Context._
import ch.wsl.box.client.views.components.Debug

case class IndexViewModel(news:Seq[Json])

object IndexViewPresenter extends ViewPresenter[IndexState.type]{

  val prop = ModelProperty{IndexViewModel(Seq())}

  override def create() = (new IndexView(prop),new IndexPresenter(prop))
}

class IndexPresenter(viewModel:ModelProperty[IndexViewModel]) extends Presenter[IndexState.type] {
  override def handleState(state: IndexState.type): Unit = {
    for{
      news <- REST.list("entity",Session.lang(),"news",JSONQuery(10,1,sort = List(JSONSort("news_id",Sort.DESC)),List()))
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


  import org.scalajs.dom.File


  private val content = div(
    h2("News"),
    Debug(viewModel),
    repeat(viewModel.subSeq(_.news)){ news =>
      div(
        div(news.get.get("news_id")),
        div(pre(news.get.get(Session.lang()))),
        br
      ).render
    }
  )


  override def getTemplate: Modifier = content

  override def renderChild(view: View): Unit = {}
}