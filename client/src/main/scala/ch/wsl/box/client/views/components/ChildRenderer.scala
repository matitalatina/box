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

      logger.debug(d.toString())
      logger.debug(metadata.get.toString)

      val widget = JSONMetadataRenderer(metadata.get, propData, children)

      childWidgets += ChildRow(widget,id)
      entity.append(id)
    }

    private def childId = UUID.randomUUID().toString

    private def attachChild(childId:String, js:Json):Json = js.deepMerge(Json.obj((ChildWidget.childTag, childId.asJson)))

    private var touched = false
    private val touchedJS = Json.obj(("$touched", true.asJson))
    private def setTouched() = if(!touched) {
      logger.debug(prop.get.toString())
      prop.get.as[Seq[Json]] match {
        case Right(js) => {
          touched = true;
          prop.set((js ++ Seq(touchedJS)).asJson)
        }
        case Left(value) => {}
      }

      logger.debug(prop.get.toString())
    }

    private def removeTouched() = if(touched){


      prop.get.as[Seq[Json]] match {
        case Right(js) => {
          touched = false;
          prop.set(js.filterNot(_ == touchedJS).asJson)
        }
        case Left(value) => {}
      }
      logger.debug(prop.get.toString())
    }


    def splitJson(js: Json): Seq[Json] = {
      js.as[Seq[Json]].right.getOrElse(Seq()) //.map{ js => js.deepMerge(Json.obj((subformInjectedId,Random.alphanumeric.take(16).mkString.asJson)))}
    }

    def mergeJson(longJs: Seq[Json]): Json = {
      longJs.asJson
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




    private def propagate[T](data: Json, metadata: JSONMetadata, f: (Widget => ((Json, JSONMetadata) => Future[T]))): Future[Seq[T]] = {

      val childMetadata = children.find(_.objId == child.objId).get

      val unorderedFormData = data.seq(child.key)
      val (existingFormData,newFormData) = unorderedFormData.zip(unorderedFormData.map(x => JSONID.fromData(x,childMetadata))).partition(_._2.isDefined)
      val orderedFormData = {existingFormData.sortBy(_._2.get)(childMetadata.order) ++ newFormData}.map(_._1)


      val (alreadyExistentData,newClientData) = childWidgets.zip(childWidgets.map(w => JSONID.fromData(w.widget.data.get,childMetadata))).partition(_._2.isDefined)

      val orderedWidgetData: Seq[ChildWidget] = {alreadyExistentData.sortBy( x => x._2.get)(childMetadata.order) ++ newClientData}.map(_._1).map(_.widget)


      val out = Future.sequence(orderedFormData.zip(orderedWidgetData).map { case (childJson, widget) =>
        f(widget)(childJson, childMetadata).map{ r =>
          r
        }
      }).map{ o =>

      // logger.info(
      //   s"""Propagate:
      //      |from server ordered:
      //      |${orderedFormData}
      //      |with Ids:
      //      |${orderedFormData.map(x => JSONID.fromData(x,childMetadata).map(_.asString))}
      //      |client data ordered
      //      |${orderedWidgetData.map(_.data.get)}
      //      |with Ids:
      //      |${orderedWidgetData.map(w => JSONID.fromData(w.data.get,childMetadata).map(_.asString))}
      //      |with propagation
      //      |$o
      //    """.stripMargin)
        o
      }
      //correct futures
      out
    }

    override def afterSave(data: Json, metadata: JSONMetadata): Future[Unit] = {
      //println(s"Propagate subform: ${subform.key} with data: $result")
      propagate(data, metadata, _.afterSave).map(_ => ())
    }

    override def beforeSave(data: Json, metadata: JSONMetadata) = {
      removeTouched()
      val js = mergeJson(childWidgets.map{ case w => w.widget.data.get })
      val mergedData = data.deepMerge(Json.obj((field.name,js)))
      logger.debug(mergedData.toString())
      propagate(mergedData, metadata, _.beforeSave).map{ jsChilds =>
        Map(child.key -> jsChilds.asJson).asJson
      }
    }

    override def killWidget(): Unit = {
      super.killWidget()
      childWidgets.foreach(_.widget.killWidget())
    }


    override def afterRender(): Unit = childWidgets.foreach(_.widget.killWidget())



//    def cleanSubwidget() = {
//      val widgetToKill = childWidgets.filterNot(w => entity.get.exists(js => w.isOf(js)))
//      childWidgets = childWidgets.filter(w => entity.get.exists(js => w.isOf(js)))
//      widgetToKill.foreach(_.killWidget())
//    }

//    def findOrAdd(write: Boolean, f: JSONMetadata, childValues: Property[Json], children: Seq[JSONMetadata]) = {
//      childWidgets.find(_._1.isOf(childValues.get)) match {
//        case None => {
//          val widget = JSONMetadataRenderer(f, childValues, children)
//          val renderedWidget = widget.render(write, Property(true))
//          val result = (widget, renderedWidget)
//          childWidgets = childWidgets ++ Seq(result)
//          result
//        }
//        case Some(cw) => {
//          if(cw._1.data.get != childValues.get) {
//            val newData  = cw._1.data.get.deepMerge(childValues.get)
//            logger.debug(s"old: ${cw._1.data.get}, new: ${newData}")
//            cw._1.data.set(newData)
//          }
//          cw
//        }
//      }
//    }





    override protected def show(): JsDom.all.Modifier = render(false)

    override protected def edit(): JsDom.all.Modifier = render(true)

    var out:Binding = null;
    var out2:Binding = null;

    private def render(write: Boolean): JsDom.all.Modifier = {

      metadata match {
        case None => p("child not found")
        case Some(f) => {

          div(
            produce(id) { _ =>
              div(
                  repeat(entity) { e =>
                    val widget = childWidgets.find(_.id == e.get)
                    val out = div(ClientConf.style.subform,
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
                    widget.get.widget.data.listen({ js =>
                      if(!js.equals(e)) {
                        //entity.replace(e.get._2, 1, js)
                        //logger.info(entity.get.toString())
                        setTouched()
                      }
                    },true)
                    out
                  }
                ).render

              },
              if (write) a(onclick :+= ((e: Event) => addItem(child, f)), Labels.subform.add) else frag()
            ).render

        }
      }
    }



    id.listen(i => {

      childWidgets.foreach(_.widget.killWidget())
      childWidgets.clear()
      entity.clear()
      val entityData = splitJson(prop.get)
      logger.debug(s"id changed with $i, new data $entityData")
      entityData.foreach(add)
    },true)



  }

}
