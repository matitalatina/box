package ch.wsl.box.client.views.components

import ch.wsl.box.client.services.REST
import ch.wsl.box.model.shared.{JSONForm, Subform}
import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import io.udash._
import org.scalajs.dom.Event

import scalatags.JsDom.all._

/**
  * Created by andre on 6/1/2017.
  */
case class SubformRenderer(result:Property[Json],label:String,subform:Subform,subforms:Seq[JSONForm]) {

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

  val model = result.transform(splitJson,mergeJson)
  val sizeModel = Property(model.get.size)

  def splitJsonFields(form:JSONForm,i:Int)(js:Seq[Json]):Seq[Json] = form.fields.map{ field =>
    js.lift(i).map(_.hcursor.get[Json](field.key).right.get).getOrElse(Json.Null)
  }
  def mergeJsonFields(form:JSONForm,i:Int)(longJs:Seq[Json]):Seq[Json] = for{
    (m,j) <- model.get.zipWithIndex
  } yield{
    if(i == j) form.fields.map(_.key).zip(longJs).toMap.asJson else m
  }

  def removeItem() = {
    println("removeItem")
    if(org.scalajs.dom.window.confirm("Are you sure?")) {
      for {
        form <- subforms.find(_.id == subform.id)
        itemToRemove <- model.get.lastOption
      } yield {
        for {
          result <- REST.delete("model", form.table, itemToRemove.keys(form.keys))
        } yield {
          if (result.count > 0) {
            model.set(model.get.init)
            sizeModel.set(sizeModel.get - 1)
          }
        }
      }
    }
  }

  def addItem() = {
    println("addItem")
  }

  def render() = {
    subforms.find(_.id == subform.id) match {
      case None => p("subform not found")
      case Some(f) => {



        div(BootstrapStyles.Panel.panel)(
          div(BootstrapStyles.Panel.panelBody, BootstrapStyles.Panel.panelDefault)(
            h4(f.name),
            produce(sizeModel) { size =>
              for {i <- 0 until size} yield {
                val subResults = model.transform(splitJsonFields(f, i), mergeJsonFields(f, i))
                JSONSchemaRenderer(f, subResults, subforms).render
              }
            },
            showIf(sizeModel.transform(_ > 0)) {
              a(onclick :+= ((e:Event) => removeItem()),"Remove").render
            },
            br,
            a(onclick :+= ((e:Event) => addItem()),"Add")

          )
        )
      }
    }
  }
}
