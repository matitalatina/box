package ch.wsl.box.client.views.components

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.utils.{Labels, Session}
import ch.wsl.box.client.views.components.widget.Widget
import ch.wsl.box.model.shared.{JSONMetadata, Subform}
import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import io.udash._
import org.scalajs.dom.Event

import scala.concurrent.Future
import scalatags.JsDom.all._

/**
  * Created by andre on 6/1/2017.
  */
case class SubformRenderer(parentData:Seq[(String,Json)],subforms:Seq[JSONMetadata]) {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._
  import io.circe._
  import io.circe.syntax._
  import ch.wsl.box.shared.utils.JsonUtils._


  def splitJson(js:Json):Seq[Json] = {
    js.as[Seq[Json]].right.getOrElse(Seq())
  }
  def mergeJson(longJs:Seq[Json]):Json = {
    longJs.asJson
  }

  def splitJsonFields(form:JSONMetadata, i:Int)(js:Seq[Json]):Seq[(String,Json)] = form.fields.flatMap{ field =>
    for{
      json <- js.lift(i)
      result <- json.hcursor.get[Json](field.key).right.toOption
    } yield field.key -> result
  }
  def mergeJsonFields(model:Property[Seq[Json]], form:JSONMetadata, i:Int)(longJs:Seq[(String,Json)]):Seq[Json] = for{
    (m,j) <- model.get.zipWithIndex
  } yield{
    if(i == j) longJs.toMap.asJson else m
  }

  def removeItem(model:Property[Seq[Json]],itemToRemove:Json,subform:Subform) = {
    println("removeItem")
    if(org.scalajs.dom.window.confirm(Labels.messages.confirm)) {
      for {
        form <- subforms.find(_.id == subform.id)
      } yield {
          model.set(model.get.filterNot(_ == itemToRemove))
      }
    }
  }


  def addItem(model:Property[Seq[Json]],sizeModel:Property[Int],subform:Subform,form:JSONMetadata) = {
    println("addItem")


    val keys = for {
      (local,sub) <- subform.localFields.split(",").zip(subform.subFields.split(","))
    } yield {
      println(s"local:$local sub:$sub")
      sub -> parentData.find(_._1 == local).map(_._2).getOrElse(Json.Null)
    }
    keys.toMap
    val placeholder:Map[String,Json] = JSONMetadata.jsonPlaceholder(form,subforms) ++ keys.toMap

    println(placeholder)

    model.set(model.get ++ Seq(placeholder.asJson))
  }

  case class SubformWidget(subform:Subform,prop:Property[Json]) extends Widget {

    val metadata = subforms.find(_.id == subform.id)

    private def propagate(result:Json,f:Widget => ((Json,JSONMetadata) => Future[Unit])) = {
      Future.sequence(subWidgets.map{ w =>
        Future.sequence(result.seq(subform.key).map{ subFormResult =>
          f(w)(subFormResult,metadata.get)
        })
      }).map(_ => ())
    }

    override def afterSave(result:Json,form:JSONMetadata): Future[Unit] = propagate(result,_.afterSave)
    override def beforeSave(result:Json,form:JSONMetadata): Future[Unit] = propagate(result,_.beforeSave)

    var subWidgets:Seq[Widget] = Seq()

    override def render() =  {

      val model: Property[Seq[Json]] = prop.transform(splitJson, mergeJson)
      val sizeModel: Property[Int] = Property(model.get.size)

      model.listen(seq => sizeModel.set(seq.size))

      metadata match {
        case None => p("subform not found")
        case Some(f) => {

          div(BootstrapStyles.Panel.panel)(
            div(BootstrapStyles.Panel.panelBody, BootstrapStyles.Panel.panelDefault)(
              h4(f.name),
              produce(sizeModel) { size =>
                subWidgets = Seq()
                for {i <- 0 until size} yield {
                  val subResults = model.transform(splitJsonFields(f, i), mergeJsonFields(model, f, i))
                  val widget = JSONSchemaRenderer(f, subResults, subforms).widget()
                  subWidgets = subWidgets ++ Seq(widget)
                  div(
                    widget.render(),
                    a(onclick :+= ((e: Event) => removeItem(model, model.get(i), subform)), Labels.subform.remove)
                  ).render
                }
              },
              br,
              a(onclick :+= ((e: Event) => addItem(model, sizeModel, subform, f)), Labels.subform.add)

            )
          )
        }
      }
    }
  }
}
