package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.model.shared.{JSONFieldOptions, JSONMetadata}
import io.circe._
import io.circe.syntax._
import io.udash.properties.single.Property
import ch.wsl.box.shared.utils.JsonUtils._

import scala.concurrent.{ExecutionContext, Future}
import scalatags.JsDom.all._

trait Widget{



  def jsonToString(json:Json):String = json.string
  def strToJson(str:String):Json = str.asJson
  def strToNumericJson(str:String):Json = str.toDouble.asJson


  def render():Modifier

  def beforeSave(result:Json,form:JSONMetadata):Future[Unit] = Future.successful(Unit)
  def afterSave(result:Json,form:JSONMetadata):Future[Unit] = Future.successful(Unit)

  protected def beforeSaveAll(result:Json,form:JSONMetadata,widgets:Seq[Widget])(implicit ec: ExecutionContext):Future[Unit] = Future.sequence(widgets.map(_.beforeSave(result,form))).map(_ => Unit)
  protected def afterSaveAll(result:Json,form:JSONMetadata,widgets:Seq[Widget])(implicit ec: ExecutionContext):Future[Unit] = Future.sequence(widgets.map(_.afterSave(result,form))).map(_ => Unit)

}

trait WidgetBinded extends Widget {

  private final val subformInjectedId = "$subform-element"

  protected def prop:Property[Json]
  private val widgetId = UUID.randomUUID().toString
  private def attachId(js:Json):Json = js.deepMerge(Json.obj((subformInjectedId,widgetId.asJson)))

  protected val propWithSubformid:Property[Json] = prop.transform(attachId,x => x)

  def isOf(js:Json) = {
    val saved = js.get(subformInjectedId)
    println(s"cheking if result subformId: $saved is equals to widgetId: $widgetId")
    js.get(subformInjectedId) == widgetId
  }
}


trait OptionWidget extends Widget{
  def options:JSONFieldOptions

  def value2Label(org:Json):String = options.options.find(_._1 == org.string).map(_._2).getOrElse("Val not found")
  def label2Value(v:String):Json = options.options.find(_._2 == v).map(_._1.asJson).getOrElse(Json.Null)
}