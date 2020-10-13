package ch.wsl.box.client.views.components.widget

import java.util.UUID

import ch.wsl.box.client.services.{Labels, REST}
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.model.shared.{JSONField, JSONFieldLookup, JSONLookup, JSONMetadata}
import io.circe._
import io.circe.syntax._
import ch.wsl.box.shared.utils.JSONUtils._
import scribe.{Logger, Logging}

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
  def afterSave(data:Json, metadata:JSONMetadata):Future[Json] = Future.successful(data)
  def afterRender():Unit = {}

  def killWidget() = {
    bindings.foreach(_.kill())
    registrations.foreach(_.cancel())
    bindings = List()
    registrations = List()
  }
  private var bindings:List[Binding] = List()
  private var registrations:List[Registration] = List()

  def autoRelease(b:Binding):Binding = {
    bindings = b :: bindings
    b
  }

  def autoRelease(r:Registration):Registration = {
    registrations = r :: registrations
    r
  }


  import scalacss.ScalatagsCss._
  import scalatags.JsDom.all._
  import io.udash.css.CssView._


  protected def saveAll(data:Json, metadata:JSONMetadata, widgets:Seq[Widget],widgetAction:Widget => (Json,JSONMetadata) => Future[Json])(implicit ec: ExecutionContext):Future[Json] = {
    widgets.foldRight(Future.successful(data)){ (widget,result) =>
      for{
        r <- result
        newResult <- widgetAction(widget)(r,metadata)
      } yield {
        r.deepMerge(newResult)
      }
    }
  }


}

trait ComponentWidgetFactory{

  def create(id:Property[Option[String]], prop:Property[Json], field:JSONField):Widget
}

object ChildWidget {
  final val childTag = "$child-element"
}

trait ChildWidget extends Widget with Logging {


  def data:Property[Json]

}


trait LookupWidget extends Widget  {

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

  private var lastQuery:Json = Json.Null

  for{
    look <- field.lookup
    query <- look.lookupQuery
  } yield {
    if(query.find(_ == '#').nonEmpty) {
      allData.listen({ json =>
        val variables = extractVariables(query)
        val queryWithSubstitutions = variables.foldRight(query){(variable, finalQuery) =>
          finalQuery.replaceAll("#" + variable, "\"" + json.js(variable).string + "\"")
        }
        val jsonQuery = parser.parse(queryWithSubstitutions) match {
          case Left(e) => {
            logger.error(e.message)
            Json.Null
          }
          case Right(j) => j
        }
        if(lastQuery != jsonQuery) {
          lastQuery = jsonQuery
          services.rest.lookup(services.clientSession.lang(),look.lookupEntity, look.map, jsonQuery).map { lookups =>
            val newLookup = toSeq(lookups)
            if (newLookup.length != lookup.get.length || newLookup.exists(lu => lookup.get.exists(_.id != lu.id))) {
              lookup.set(newLookup, true)
            }
          }
        }
      }, true)
    }
  }

  private def extractVariables(query:String):Seq[String] = {
    query.zipWithIndex.filter(_._1 == '#').map{ case (_,i) =>
      val nextIndex = Seq(query.length,query.indexOf(' ',i),query.indexOf('}',i),query.indexOf(',',i)).min
      query.substring(i+1,nextIndex).replaceAll("\n","").trim
    }.distinct
  }



  def value2Label(org:Json):String = lookup.get.find(_.id == org.string).map(_.value).orElse(field.lookup.get.lookup.find(_.id == org.string).map(_.value)).getOrElse(Labels.lookup.not_found)
  def label2Value(v:String):Json = lookup.get.find(_.value == v).map(_.id.asJson).orElse(field.lookup.get.lookup.find(_.value == v).map(_.id.asJson)).getOrElse(Json.Null)
}
