package ch.wsl.box.client.views.components.widget

import ch.wsl.box.model.shared.JSONFieldOptions
import io.circe._
import io.circe.syntax._
import io.udash.properties.single.Property
import ch.wsl.box.shared.utils.JsonUtils._


trait Widget{
  import scalatags.JsDom.all._


  def jsonToString(json:Json):String = json.string
  def strToJson(str:String):Json = str.asJson
  def strToNumericJson(str:String):Json = str.toDouble.asJson

  type WidgetContent = Modifier

  def render(key:Property[String],label:String,prop:Property[Json]):WidgetContent
}

trait OptionWidget extends Widget{
  def options:JSONFieldOptions

  def value2Label(org:Json):String = options.options.find(_._1 == org.string).map(_._2).getOrElse("Val not found")
  def label2Value(v:String):Json = options.options.find(_._2 == v).map(_._1.asJson).getOrElse(Json.Null)
}