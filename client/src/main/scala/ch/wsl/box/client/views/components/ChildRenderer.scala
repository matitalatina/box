package ch.wsl.box.client.views.components

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.utils.{Labels, Session}
import ch.wsl.box.client.views.components.widget.{Widget, WidgetBinded}
import ch.wsl.box.model.shared.{JSONMetadata, Child}
import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import io.udash._
import org.scalajs.dom.Event

import scala.concurrent.Future
import scala.util.Random
import scalatags.JsDom.all._

/**
  * Created by andre on 6/1/2017.
  */
case class ChildRenderer(child:Child, children:Seq[JSONMetadata], prop:Property[Json], masterData:Property[Json]) extends Widget {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe._
  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JsonUtils._



  def splitJson(js:Json):Seq[Json] = {
    js.as[Seq[Json]].right.getOrElse(Seq())//.map{ js => js.deepMerge(Json.obj((subformInjectedId,Random.alphanumeric.take(16).mkString.asJson)))}
  }
  def mergeJson(longJs:Seq[Json]):Json = {
    longJs.asJson
  }

  def splitJsonFields(metadata:JSONMetadata, i:Int)(js:Seq[Json]):Json = js.lift(i).getOrElse(Json.Null)
  def mergeJsonFields(entity:Property[Seq[Json]], metadata:JSONMetadata, i:Int)(longJs:Json):Seq[Json] = for{
    (m,j) <- entity.get.zipWithIndex
  } yield{
    if(i == j) longJs else m
  }

  def removeItem(entity:Property[Seq[Json]], itemToRemove:Json, child:Child) = {
    println("removing item")
    if(org.scalajs.dom.window.confirm(Labels.messages.confirm)) {
      for {
        form <- children.find(_.objId == child.objId)
      } yield {
          entity.set(entity.get.filterNot(_ == itemToRemove))
      }
    }
  }


  def addItem(entity:Property[Seq[Json]], child:Child, metadata:JSONMetadata) = {
    println("adding item")


    val keys = for {
      (local,sub) <- child.masterFields.split(",").zip(child.childFields.split(","))
    } yield {
//      println(s"local:$local sub:$sub")
      sub -> masterData.get.js(local)
    }
    keys.toMap
    val placeholder:Map[String,Json] = JSONMetadata.jsonPlaceholder(metadata,children) ++ keys.toMap

//    println(placeholder)


    entity.set(entity.get ++ Seq(placeholder.asJson))
  }

    val metadata = children.find(_.objId == child.objId)

    private def propagate(data:Json,metadata:JSONMetadata,f:(Widget => ((Json,JSONMetadata) => Future[Unit]))):Future[Unit] = {
          val out = data.seq(child.key).zip(childWidgets).map { case (childJson,widget) =>
                //println(s"Propagate subform element: ${subform.key} with data: $subformJson")
                f(widget)(childJson,children.find(_.objId == child.objId).get)
            }

          //correct futures
          Future.sequence(out).map(_ => ())
    }

    override def afterSave(data:Json, metadata:JSONMetadata): Future[Unit] = {
      //println(s"Propagate subform: ${subform.key} with data: $result")
      propagate(data,metadata,_.afterSave)
    }
    override def beforeSave(data:Json, metadata:JSONMetadata): Future[Unit] = propagate(data,metadata,_.beforeSave)

    var childWidgets:Seq[WidgetBinded] = Seq()

    def cleanSubwidget() = {
      childWidgets = childWidgets.filter(w => entity.get.exists(js => w.isOf(js)))
    }
    def findOrAdd(f:JSONMetadata, childValues:Property[Json], children: Seq[JSONMetadata]) = {
      childWidgets.find(_.isOf(childValues.get)).getOrElse {
        val widget = JSONMetadataRenderer(f, childValues, children)
        childWidgets = childWidgets ++ Seq(widget)
        widget
      }
    }


    val entity: Property[Seq[Json]] = prop.transform(splitJson, mergeJson)
    val entitySize: Property[Int] = Property(entity.get.size)

    override def render() =  {

      entity.listen(seq => entitySize.set(seq.size))

      metadata match {
        case None => p("child not found")
        case Some(f) => {

          div(BootstrapStyles.Panel.panel)(
            div(BootstrapStyles.Panel.panelBody, BootstrapStyles.Panel.panelDefault)(
              h4(f.name),
              produce(entitySize) { size =>
                cleanSubwidget()
                for {i <- 0 until size} yield {
                  val subResults = entity.transform(splitJsonFields(f, i), mergeJsonFields(entity, f, i))
                  val widget = findOrAdd(f, subResults, children)
                  div(
                    widget.render(),
                    a(onclick :+= ((e: Event) => removeItem(entity, entity.get(i), child)), Labels.subform.remove)
                  ).render
                }
              },
              br,
              a(onclick :+= ((e: Event) => addItem(entity, child, f)), Labels.subform.add)

            )
          )
        }
      }
    }

}
