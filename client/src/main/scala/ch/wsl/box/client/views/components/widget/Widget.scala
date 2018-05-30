package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.model.shared.{JSONFieldLookup, JSONMetadata}
import io.circe._
import io.circe.syntax._
import io.udash.properties.single.{Property, ReadableProperty}
import ch.wsl.box.shared.utils.JsonUtils._
import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}
import scalatags.JsDom.all._
import io.udash._

trait Widget{

  def jsonToString(json:Json):String = json.string
  def strToJson(str:String):Json = str.asJson
  def strToNumericJson(str:String):Json = str.toDouble.asJson

  protected def render():Modifier
  def render(conditional:ReadableProperty[Boolean]):Modifier = showIf(conditional) {
    div(render()).render
  }

  def beforeSave(data:Json, metadata:JSONMetadata):Future[Unit] = Future.successful(Unit)
  def afterSave(data:Json, metadata:JSONMetadata):Future[Unit] = Future.successful(Unit)

  protected def beforeSaveAll(data:Json, metadata:JSONMetadata, widgets:Seq[Widget])(implicit ec: ExecutionContext):Future[Unit] = Future.sequence(widgets.map(_.beforeSave(data,metadata))).map(_ => Unit)
  protected def afterSaveAll(data:Json, metadata:JSONMetadata, widgets:Seq[Widget])(implicit ec: ExecutionContext):Future[Unit] = Future.sequence(widgets.map(_.afterSave(data,metadata))).map(_ => Unit)

}

trait WidgetBinded extends Widget with Logging {

  private final val childInjectedId = "$child-element"

  protected def data:Property[Json]
  private val widgetId = UUID.randomUUID().toString
  private def attachId(js:Json):Json = js.deepMerge(Json.obj((childInjectedId, widgetId.asJson)))

  protected val dataWithChildId:Property[Json] = data.transform(attachId, x => x)

  def isOf(js:Json) = {
    val saved = js.get(childInjectedId)
    logger.debug(s"cheking if result childId: $saved is equals to widgetId: $widgetId")
    js.get(childInjectedId) == widgetId
  }
}


trait LookupWidget extends Widget{
  def lookup:JSONFieldLookup

  def value2Label(org:Json):String = lookup.lookup.find(_.id == org.string).map(_.value).getOrElse("Value not found")
  def label2Value(v:String):Json = lookup.lookup.find(_.value == v).map(_.id.asJson).getOrElse(Json.Null)
}