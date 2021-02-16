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
import io.udash.properties.single.Property
import org.scalajs.dom.Element
import org.scalajs.dom

import scala.concurrent.duration._

trait Widget extends Logging {

  def field:JSONField

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

  def showOnTable():Modifier = frag("Not implemented")
  def editOnTable():Modifier = frag("Not implemented")

  def render(write:Boolean,conditional:ReadableProperty[Boolean]):Modifier = showIf(conditional) {
    if(write && !field.readOnly) {
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

trait HasData extends Widget {
  def data:Property[Json]

  override def showOnTable(): JsDom.all.Modifier = autoRelease(bind(data.transform(_.string)))

}



case class WidgetParams(
                         id:Property[Option[String]],
                         prop:Property[Json],
                         field:JSONField,
                         metadata: JSONMetadata,
                         allData:Property[Json],
                         children:Seq[JSONMetadata]
                       )

trait ComponentWidgetFactory{

  def name:String

  def create(params:WidgetParams):Widget
}

object ChildWidget {
  final val childTag = "$child-element"
}

trait ChildWidget extends Widget with HasData


trait LookupWidget extends Widget with HasData {

  import ch.wsl.box.client.Context._

  def allData:Property[Json]

  def field:JSONField
  val lookup:SeqProperty[JSONLookup] = {
    SeqProperty(toSeq(field.lookup.toSeq.flatMap(_.lookup)))
  }

  val model:Property[JSONLookup] = Property(JSONLookup("",""))

  autoRelease(field.`type` match {
    case "number" =>  data.sync[JSONLookup](model)(
      {json:Json =>
        logger.debug(json.toString())
        val id = jsonToString(json)
        lookup.get.find(_.id == jsonToString(json)).getOrElse(JSONLookup(id,id + " NOT FOUND"))
      },
      {jsonLookup:JSONLookup => strToNumericJson(jsonLookup.id)}
    )
    case _ => data.sync[JSONLookup](model)(
      {json:Json =>
        logger.debug(json.toString())
        val id = jsonToString(json)
        logger.debug(id.toString())
        logger.debug(lookup.toString())
        logger.debug(lookup.get.toString())
        val result = lookup.get.find(_.id == id).getOrElse(JSONLookup(id,id + " NOT FOUND"))
        logger.debug(result.toString)
        result
      },
      {jsonLookup:JSONLookup => strToJson(field.nullable)(jsonLookup.id)}
    )
  })



  val selectModel = Property("")
  autoRelease(data.sync(selectModel)(value2Label,label2Value))


  override def showOnTable(): JsDom.all.Modifier = autoRelease(bind(selectModel))




  private def toSeq(s:Seq[JSONLookup]):Seq[JSONLookup] = if(field.nullable) {
    Seq(JSONLookup("","")) ++ s
  } else {
    s
  }

  private def setNewLookup(newLookup:Seq[JSONLookup]) = {
    logger.info(newLookup.toString())
    if (newLookup.length != lookup.get.length || newLookup.exists(lu => lookup.get.exists(_.id != lu.id))) {
      logger.info("Lookup list changed")
      lookup.set(newLookup, true)
      if(!newLookup.exists(_.id == data.get.string)) {
        logger.info("Old value not exists")
        newLookup.headOption.foreach{x =>
          logger.info(s"Setting model to $x")
          model.set(x,true)
        }
      }

    }
  }

  field.lookup.get.lookupExtractor.foreach{case extractor =>
    allData.listen({ all =>
      logger.debug(all.toString())
      val newLookup = toSeq(extractor.map.getOrElse(all.js(extractor.key), Seq()))
      setNewLookup(newLookup)
    },true)
  }


  for{
    look <- field.lookup
    query <- look.lookupQuery
  } yield {
    if(query.find(_ == '#').nonEmpty) {

      val variables =extractVariables(query)
      val queryWithSubstitutions = allData.transform({ json =>
        variables.foldRight(query){(variable, finalQuery) =>
          finalQuery.replaceAll("#" + variable, "\"" + json.js(variable).string + "\"")
        }
      })
      queryWithSubstitutions.listen({ q =>
        lookup.set(Seq(), true) //reset lookup state

        val jsonQuery = parser.parse(q) match {
          case Left(e) => {
            logger.error(e.message)
            Json.Null
          }
          case Right(j) => j
        }

        services.rest.lookup(services.clientSession.lang(),look.lookupEntity, look.map, jsonQuery).map { lookups =>
          setNewLookup(toSeq(lookups))
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



  private def value2Label(org:Json):String = {

    val lookupValue = allData.get.get(field.lookup.get.map.localValueProperty)

    lookup.get.find(_.id == lookupValue).map(_.value)
      .orElse(field.lookup.get.lookup.find(_.id == org.string).map(_.value))
      .getOrElse(Labels.lookup.not_found)
  }
  private def label2Value(v:String):Json = lookup.get.find(_.value == v).map(_.id.asJson).orElse(field.lookup.get.lookup.find(_.value == v).map(_.id.asJson)).getOrElse(Json.Null)
}
