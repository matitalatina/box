package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.Labels
import ch.wsl.box.model.shared.{JSONField, JSONFieldLookup, JSONLookup, JSONMetadata}
import io.circe._
import io.circe.syntax._
import ch.wsl.box.shared.utils.JSONUtils._
import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}
import scalatags.JsDom.all._
import scalatags.JsDom
import io.udash._
import io.udash.bindings.Bindings
import io.udash.bindings.modifiers.Binding
import io.udash.bootstrap.tooltip.UdashTooltip
import org.scalajs.dom.Element
import org.scalajs.dom

import scala.concurrent.duration._

trait Widget extends Logging {

  def jsonToString(json:Json):String = json.string

  def strToJson(nullable:Boolean = false)(str:String):Json = (str, nullable) match {
    case ("", true) => Json.Null
    case _ => str.asJson
  }

  def strToNumericJson(str:String):Json = str match {
    case "" => Json.Null
    case _ => str.toDouble.asJson
  }

  def strToNumericArrayJson(str:String):Json = str match {
    case "" => Json.Null
    case _ => str.asJson.asArray.map(_.map(s => strToNumericJson(s.string))).map(_.asJson).getOrElse(Json.Null)
  }

  protected def show():Modifier
  protected def edit():Modifier

  def render(write:Boolean,conditional:ReadableProperty[Boolean]):Modifier = showIf(conditional) {
    if(write) {
      div(edit()).render
    } else {
      div(show()).render
    }
  }

  def beforeSave(data:Json, metadata:JSONMetadata):Future[Json] = Future.successful(data)
  def afterSave(data:Json, metadata:JSONMetadata):Future[Unit] = Future.successful(Unit)
  def afterRender():Unit = {}

  def killWidget() = {
    bindings.foreach(_.kill())
  }
  private var bindings:List[Binding] = List()

  def autoRelease(b:Binding):Binding = {
    bindings = b :: bindings
    b
  }


  import scalacss.ScalatagsCss._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._


  protected def beforeSaveAll(data:Json, metadata:JSONMetadata, widgets:Seq[Widget])(implicit ec: ExecutionContext):Future[Json] = Future.sequence(widgets.map(_.beforeSave(data,metadata))).map{ fields =>
    fields.foldRight(JsonObject.empty.asJson){ (a,result) =>
      logger.info(s"merging $a into $result")
      val r = a.deepMerge(result)
      logger.info(s"result: $r")
      r
    }
  }
  protected def afterSaveAll(data:Json, metadata:JSONMetadata, widgets:Seq[Widget])(implicit ec: ExecutionContext):Future[Unit] = Future.sequence(widgets.map(_.afterSave(data,metadata))).map(_ => Unit)


}

trait ComponentWidgetFactory{

  def create(id:Property[String], prop:Property[Json], field:JSONField):Widget
}


trait ChildWidget extends Widget with Logging {

  private final val childTag = "$child-element"

  def data:Property[Json]

  private val childId = UUID.randomUUID().toString

  private def attachChild(js:Json):Json = js.deepMerge(Json.obj((childTag, childId.asJson)))

  protected val dataWithChildId:Property[Json] = data.transform(attachChild, x => x)

  def isOf(js:Json) = {
    val saved = js.get(childTag)
    logger.debug(s"cheking if result childId: $saved is equals to widgetId: $childId")
    saved == childId
  }
}


trait LookupWidget extends Widget {

  import ch.wsl.box.client.Context._

  def allData:Property[Json]


  def field:JSONField
  val lookup:SeqProperty[JSONLookup] = {
    SeqProperty(toSeq(field.lookup.get.lookup))
  }

  private def toSeq(s:Seq[JSONLookup]):Seq[JSONLookup] = if(field.nullable) {
    Seq(JSONLookup("","")) ++ s
  } else {
    s
  }

  for{
    look <- field.lookup
    query <- look.lookupQuery
  } yield {
    if(query.find(_ == '#').nonEmpty) {
      allData.listen({ json =>
        val variables = extractVariables(query)
        val queryWithSubstitutions = variables.foldRight(query)((variable, finalQuery) => finalQuery.replaceAll("#" + variable, json.js(variable).toString()))
        REST.lookup(look.lookupEntity, look.map, parser.parse(queryWithSubstitutions).right.get).map { lookups =>
          val newLookup = toSeq(lookups)
          if (newLookup.length != lookup.get.length || newLookup.exists(lu => lookup.get.exists(_.id != lu.id))) {
            lookup.set(newLookup, true)
          }
        }
      }, true)
    }
  }

  private def extractVariables(query:String):Seq[String] = {
    query.zipWithIndex.filter(_._1 == '#').map{ case (_,i) =>
      val nextIndex = Seq(query.length,query.indexOf(' ',i),query.indexOf('}',i),query.indexOf(',',i)).min
      query.substring(i+1,nextIndex)
    }.distinct
  }



  def value2Label(org:Json):String = lookup.get.find(_.id == org.string).map(_.value).orElse(field.lookup.get.lookup.find(_.id == org.string).map(_.value)).getOrElse(Labels.lookup.not_found)
  def label2Value(v:String):Json = lookup.get.find(_.value == v).map(_.id.asJson).orElse(field.lookup.get.lookup.find(_.value == v).map(_.id.asJson)).getOrElse(Json.Null)
}
