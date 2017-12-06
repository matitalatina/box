package ch.wsl.box.client.views.components

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.utils.{Labels, Session}
import ch.wsl.box.client.views.components.widget.{Widget, WidgetBinded}
import ch.wsl.box.model.shared.{JSONMetadata, Subform}
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
case class SubformRenderer(subform:Subform,prop:Property[Json],parentData:Property[Json],subforms:Seq[JSONMetadata]) extends Widget {

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

  def splitJsonFields(form:JSONMetadata, i:Int)(js:Seq[Json]):Json = js.lift(i).getOrElse(Json.Null)
  def mergeJsonFields(entity:Property[Seq[Json]], form:JSONMetadata, i:Int)(longJs:Json):Seq[Json] = for{
    (m,j) <- entity.get.zipWithIndex
  } yield{
    if(i == j) longJs else m
  }

  def removeItem(entity:Property[Seq[Json]], itemToRemove:Json, subform:Subform) = {
    println("removeItem")
    if(org.scalajs.dom.window.confirm(Labels.messages.confirm)) {
      for {
        form <- subforms.find(_.id == subform.id)
      } yield {
          entity.set(entity.get.filterNot(_ == itemToRemove))
      }
    }
  }


  def addItem(entity:Property[Seq[Json]], subform:Subform, form:JSONMetadata) = {
    println("addItem")


    val keys = for {
      (local,sub) <- subform.localFields.split(",").zip(subform.subFields.split(","))
    } yield {
      println(s"local:$local sub:$sub")
      sub -> parentData.get.js(local)
    }
    keys.toMap
    val placeholder:Map[String,Json] = JSONMetadata.jsonPlaceholder(form,subforms) ++ keys.toMap

    println(placeholder)


    entity.set(entity.get ++ Seq(placeholder.asJson))
  }

    val metadata = subforms.find(_.id == subform.id)

    private def propagate(result:Json,form:JSONMetadata,f:(Widget => ((Json,JSONMetadata) => Future[Unit]))):Future[Unit] = {
          val out = result.seq(subform.key).zip(subWidgets).map { case (subformJson,schemaRenderer) =>
                //println(s"Propagate subform element: ${subform.key} with data: $subformJson")
                f(schemaRenderer)(subformJson,subforms.find(_.id == subform.id).get)
            }

          //correct futures
          Future.sequence(out).map(_ => ())
    }

    override def afterSave(result:Json,form:JSONMetadata): Future[Unit] = {
      //println(s"Propagate subform: ${subform.key} with data: $result")
      propagate(result,form,_.afterSave)
    }
    override def beforeSave(result:Json,form:JSONMetadata): Future[Unit] = propagate(result,form,_.beforeSave)

    var subWidgets:Seq[WidgetBinded] = Seq()

    def cleanSubwidget() = {
      subWidgets = subWidgets.filter(w => entity.get.exists(js => w.isOf(js)))
    }
    def findOrAdd(f:JSONMetadata,subResults:Property[Json],subforms: Seq[JSONMetadata]) = {
      subWidgets.find(_.isOf(subResults.get)).getOrElse {
        val widget = JSONSchemaRenderer(f, subResults, subforms)
        subWidgets = subWidgets ++ Seq(widget)
        widget
      }
    }


    val entity: Property[Seq[Json]] = prop.transform(splitJson, mergeJson)
    val entitySize: Property[Int] = Property(entity.get.size)

    override def render() =  {

      entity.listen(seq => entitySize.set(seq.size))

      metadata match {
        case None => p("subform not found")
        case Some(f) => {

          div(BootstrapStyles.Panel.panel)(
            div(BootstrapStyles.Panel.panelBody, BootstrapStyles.Panel.panelDefault)(
              h4(f.name),
              produce(entitySize) { size =>
                cleanSubwidget()
                for {i <- 0 until size} yield {
                  val subResults = entity.transform(splitJsonFields(f, i), mergeJsonFields(entity, f, i))
                  val widget = findOrAdd(f, subResults, subforms)
                  div(
                    widget.render(),
                    a(onclick :+= ((e: Event) => removeItem(entity, entity.get(i), subform)), Labels.subform.remove)
                  ).render
                }
              },
              br,
              a(onclick :+= ((e: Event) => addItem(entity, subform, f)), Labels.subform.add)

            )
          )
        }
      }
    }

}
