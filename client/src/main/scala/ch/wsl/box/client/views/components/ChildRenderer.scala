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
import scala.util.Random
import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import scalatags.JsDom

/**
  * Created by andre on 6/1/2017.
  */

case class ChildRendererFactory(child:Child, children:Seq[JSONMetadata], masterData:Property[Json]) extends ComponentWidgetFactory {


  override def create(id: Property[String], prop: Property[Json], field: JSONField): Widget = ChildRenderer(id,prop,field)

  case class ChildRenderer(id: Property[String], prop: Property[Json], field:JSONField) extends Widget with Logging {

    import ch.wsl.box.client.Context._
    import scalatags.JsDom.all._
    import io.udash.css.CssView._
    import io.circe._
    import io.circe.syntax._
    import ch.wsl.box.shared.utils.JSONUtils._

    var childWidgets: Seq[ChildWidget] = Seq()
    val entity: SeqProperty[Json] = SeqProperty(Seq())

    private def childId = UUID.randomUUID().toString

    private def attachChild(js:Json):Json = js.deepMerge(Json.obj((ChildWidget.childTag, childId.asJson)))


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

    def removeItem(itemToRemove: Json, child: Child) = {
      logger.info("removing item")
      if (org.scalajs.dom.window.confirm(Labels.messages.confirm)) {
        for {
          form <- children.find(_.objId == child.objId)
        } yield {
          entity.remove(itemToRemove)
        }
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


      entity.append(attachChild(placeholder.asJson))
    }

    val metadata = children.find(_.objId == child.objId)


    private def propagate[T](data: Json, metadata: JSONMetadata, f: (Widget => ((Json, JSONMetadata) => Future[T]))): Future[Seq[T]] = {

      val childMetadata = children.find(_.objId == child.objId).get

      val unorderedFormData = data.seq(child.key)
      val (existingFormData,newFormData) = unorderedFormData.zip(unorderedFormData.map(x => JSONID.fromData(x,childMetadata))).partition(_._2.isDefined)
      val orderedFormData = {existingFormData.sortBy(_._2.get)(childMetadata.order) ++ newFormData}.map(_._1)


      val (alreadyExistentData,newClientData) = childWidgets.zip(childWidgets.map(w => JSONID.fromData(w.data.get,childMetadata))).partition(_._2.isDefined)

      val orderedWidgetData: Seq[ChildWidget] = {alreadyExistentData.sortBy( x => x._2.get)(childMetadata.order) ++ newClientData}.map(_._1)


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

    override def beforeSave(data: Json, metadata: JSONMetadata) = propagate(data, metadata, _.beforeSave).map{ jsChilds =>
      Map(child.key -> jsChilds.asJson).asJson
    }

    override def killWidget(): Unit = {
      super.killWidget()
      childWidgets.foreach(_.killWidget())
    }


    override def afterRender(): Unit = childWidgets.foreach(_.killWidget())



//    def cleanSubwidget() = {
//      val widgetToKill = childWidgets.filterNot(w => entity.get.exists(js => w.isOf(js)))
//      childWidgets = childWidgets.filter(w => entity.get.exists(js => w.isOf(js)))
//      widgetToKill.foreach(_.killWidget())
//    }

    def findOrAdd(f: JSONMetadata, childValues: Property[Json], children: Seq[JSONMetadata]) = {
      childWidgets.find(_.isOf(childValues.get)).getOrElse {
        val widget = JSONMetadataRenderer(f, childValues, children)
        childWidgets = childWidgets ++ Seq(widget)
        widget
      }
    }





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
                  repeat(entity.zipWithIndex) { e =>
                    val sub = Property(e.get._1)

                    val widget = findOrAdd(f,sub, children)
                    val out = div(ClientConf.style.subform,
                      widget.render(write, Property(true)),
                      if (write) div(
                        BootstrapStyles.row,
                        div(BootstrapCol.md(12), ClientConf.style.block,
                          div(BootstrapStyles.pullRight,
                            a(onclick :+= ((_: Event) => removeItem(e.get._1, child)), Labels.subform.remove)
                          )
                        )
                      ) else frag()
                    ).render
                    sub.listen { js =>
                      if(!js.equals(e.get._1)) {
                        entity.replace(e.get._2, 1, js)
                        logger.info(entity.get.toString())
                      }
                    }
                    out
                  }
                ).render

              },
              if (write) a(onclick :+= ((e: Event) => addItem(child, f)), Labels.subform.add) else frag()
            ).render

        }
      }
    }


    var firstRun = true;

    id.listen(_ => {
      if(firstRun) {
        firstRun = false;
      } else {
//        out.kill()
//        out2.kill()
      }

      childWidgets.foreach(_.killWidget())
      childWidgets = Seq()

      val seq = splitJson(prop.get)
      entity.set(seq.map(attachChild))
      entity.clearListeners()
      entity.listen({ seq =>
        prop.set(mergeJson(seq))
      },true)
    },true)



  }

}
