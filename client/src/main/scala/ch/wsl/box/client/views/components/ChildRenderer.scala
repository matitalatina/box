package ch.wsl.box.client.views.components

import java.util.UUID

import ch.wsl.box.client.services.REST
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.{ClientConf, Labels, Session}
import ch.wsl.box.client.views.components.widget.{ChildWidget, ComponentWidgetFactory, Widget}
import ch.wsl.box.model.shared._
import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.properties.single.Property
import io.udash._
import io.udash.bindings.modifiers.Binding
import org.scalajs.dom.Event
import scribe.Logging

import scala.concurrent.Future
import scala.util.{Random, Success}
import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import scalatags.JsDom

/**
  * Created by andre on 6/1/2017.
  */

case class ChildRow(widget:ChildWidget,id:String)

case class ChildRendererFactory(child:Child, children:Seq[JSONMetadata], masterData:Property[Json]) extends ComponentWidgetFactory {


  override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = ChildRenderer(id,prop,field)

  case class ChildRenderer(id: Property[String], prop: Property[Json], field:JSONField) extends Widget with Logging {

    import ch.wsl.box.client.Context._
    import scalatags.JsDom.all._
    import io.udash.css.CssView._
    import io.circe._
    import io.circe.syntax._
    import ch.wsl.box.shared.utils.JSONUtils._

    val childWidgets: scala.collection.mutable.ListBuffer[ChildRow] = scala.collection.mutable.ListBuffer()
    val entity: SeqProperty[String] = SeqProperty(Seq())
    val metadata = children.find(_.objId == child.objId)

    private def add(data:Json): Unit = {
      val id = childId
      val d = attachChild(id,data)
      val propData = Property(d)


      val widget = JSONMetadataRenderer(metadata.get, propData, children)

      childWidgets += ChildRow(widget,id)
      entity.append(id)
    }

    private def childId = UUID.randomUUID().toString

    private def attachChild(childId:String, js:Json):Json = js.deepMerge(Json.obj((ChildWidget.childTag, childId.asJson)))


    def splitJson(js: Json): Seq[Json] = {
      js.as[Seq[Json]].right.getOrElse(Seq()) //.map{ js => js.deepMerge(Json.obj((subformInjectedId,Random.alphanumeric.take(16).mkString.asJson)))}
    }


//    def splitJsonRows(metadata: JSONMetadata, i: Int)(js: Seq[Json]): Json = js.lift(i).getOrElse(Json.Null)
//
//    def mergeJsonRows(entity: Property[Seq[Json]], metadata: JSONMetadata, i: Int)(longJs: Json): Seq[Json] = for {
//      (m, j) <- entity.get.zipWithIndex
//    } yield {
//      if (i == j) longJs else m
//    }

    def removeItem(itemToRemove: String) = {
      logger.info("removing item")
      if (org.scalajs.dom.window.confirm(Labels.messages.confirm)) {
        entity.remove(itemToRemove)
        childWidgets.remove(childWidgets.zipWithIndex.find(x => x._1.id == itemToRemove).get._2)
      }
    }


    def addItem(child: Child, metadata: JSONMetadata) = {
      logger.info("adding item")


      val keys = for {
        (local, sub) <- child.masterFields.split(",").zip(child.childFields.split(","))
      } yield {
        //      println(s"local:$local sub:$sub")
        sub -> masterData.get.js(local)
      }
      keys.toMap
      val placeholder: Map[String, Json] = JSONMetadata.jsonPlaceholder(metadata, children) ++ keys.toMap

      //    println(placeholder)


      add(placeholder.asJson)
    }




    private def propagate[T](data: Json,f: (Widget => ((Json, JSONMetadata) => Future[T]))): Future[Seq[T]] = {

      val childMetadata = children.find(_.objId == child.objId).get

      val rows = data.seq(child.key)


      val out = Future.sequence(childWidgets.zipWithIndex.map { case (cw,i) =>

        val d = rows.lift(i).getOrElse(Json.Null).deepMerge(cw.widget.data.get)

        f(cw.widget)(d, childMetadata).map{ r =>
          r
        }
      })
      //correct futures
      out
    }

    def collectData(jsChilds:Seq[Json]) = {
      logger.debug(Map(child.key -> jsChilds.asJson).asJson.toString())
      Map(child.key -> jsChilds.asJson).asJson
    }

    override def afterSave(data: Json, metadata: JSONMetadata): Future[Json] = {
      propagate(data, _.afterSave).map(collectData)
    }

    override def beforeSave(data: Json, metadata: JSONMetadata) = {
      propagate(data, _.beforeSave).map(collectData)
    }

    override def killWidget(): Unit = {
      super.killWidget()
      childWidgets.foreach(_.widget.killWidget())
    }


    override def afterRender(): Unit = childWidgets.foreach(_.widget.afterRender())


    override protected def show(): JsDom.all.Modifier = render(false)

    override protected def edit(): JsDom.all.Modifier = render(true)


    private def render(write: Boolean): JsDom.all.Modifier = {

      metadata match {
        case None => p("child not found")
        case Some(f) => {

          div(
            div(
              autoRelease(repeat(entity) { e =>
                val widget = childWidgets.find(_.id == e.get)
                div(ClientConf.style.subform,
                  widget.get.widget.render(write, Property(true)),
                  if (write) div(
                    BootstrapStyles.row,
                    div(BootstrapCol.md(12), ClientConf.style.block,
                      div(BootstrapStyles.pullRight,
                        a(onclick :+= ((_: Event) => removeItem(e.get)), Labels.subform.remove)
                      )
                    )
                  ) else frag()
                ).render
              })
            ).render,
            if (write) a(onclick :+= ((e: Event) => addItem(child, f)), Labels.subform.add) else frag()
          )

        }
      }
    }



    autoRelease(id.listen(i => {

      childWidgets.foreach(_.widget.killWidget())
      childWidgets.clear()
      entity.clear()
      val entityData = splitJson(prop.get)
      logger.debug(s"id changed with $i")
      entityData.foreach(add)
    },true))




  }

}
