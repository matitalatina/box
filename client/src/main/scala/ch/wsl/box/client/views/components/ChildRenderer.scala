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

case class ChildRow(widget:ChildWidget,id:String, data:ReadableProperty[Json], open:Boolean)

trait ChildRendererFactory extends ComponentWidgetFactory {

  def child:Child
  def children:Seq[JSONMetadata]
  def masterData:Property[Json]

  trait ChildRenderer extends Widget with Logging {

    import ch.wsl.box.client.Context._
    import scalatags.JsDom.all._
    import io.udash.css.CssView._
    import io.circe._
    import io.circe.syntax._
    import ch.wsl.box.shared.utils.JSONUtils._

    def id: Property[String]
    def prop: Property[Json]
    def field:JSONField

    val childWidgets: scala.collection.mutable.ListBuffer[ChildRow] = scala.collection.mutable.ListBuffer()
    val entity: SeqProperty[String] = SeqProperty(Seq())
    val metadata = children.find(_.objId == child.objId)

    protected def render(write: Boolean): JsDom.all.Modifier

    private def add(data:Json,open:Boolean): Unit = {
      val id = childId
      val d = attachChild(id,data)
      val propData = Property(d)


      val widget = JSONMetadataRenderer(metadata.get, propData, children)


      childWidgets += ChildRow(widget,id,propData,open)
      entity.append(id)
      widget.afterRender()
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


      add(placeholder.asJson,true)
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




    autoRelease(id.listen(i => {

      childWidgets.foreach(_.widget.killWidget())
      childWidgets.clear()
      entity.clear()
      val entityData = splitJson(prop.get)
      logger.debug(s"id changed with $i")
      entityData.foreach(x => add(x,false))
    },true))




  }


}

case class SimpleChildFactory(child:Child, children:Seq[JSONMetadata], masterData:Property[Json]) extends ChildRendererFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = SimpleChildRenderer(id,prop,field)

  case class SimpleChildRenderer(id: Property[String], prop: Property[Json], field:JSONField) extends ChildRenderer {

    import scalatags.JsDom.all._
    import io.udash.css.CssView._

    override protected def render(write: Boolean): JsDom.all.Modifier = {

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
                    BootstrapStyles.Grid.row,
                    div(BootstrapCol.md(12), ClientConf.style.block,
                      div(BootstrapStyles.Float.right(),
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
  }


}


case class TableChildFactory(child:Child, children:Seq[JSONMetadata], masterData:Property[Json]) extends ChildRendererFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = TableChildRenderer(id,prop,field)

  case class TableChildRenderer(id: Property[String], prop: Property[Json], field:JSONField) extends ChildRenderer {

    import scalatags.JsDom.all._
    import io.udash.css.CssView._
    import ch.wsl.box.shared.utils.JSONUtils._

    override protected def render(write: Boolean): JsDom.all.Modifier = {

      metadata match {
        case None => p("child not found")
        case Some(f) => {

          val fields = f.tabularFields.flatMap{fieldId => f.fields.find(_.name == fieldId)}

          div(
            table(ClientConf.style.childTable,
              tr(ClientConf.style.childTableTr,ClientConf.style.childTableHeader,
                fields.map(f => td(ClientConf.style.childTableTd,f.title)),
                td()
              ),
              tbody(
                autoRelease(repeat(entity) { e =>
                  val widget = childWidgets.find(_.id == e.get)
                  val open = Property(widget.get.open)
                  frag(
                    tr(ClientConf.style.childTableTr,
                      produce(widget.get.data) { data  => fields.map(x => td(ClientConf.style.childTableTd,data.get(x.name))).render},
                      td(ClientConf.style.childTableTd, ClientConf.style.childTableAction,a(produce(open) {
                        case true => StringFrag("Close").render
                        case false => StringFrag("Open").render
                      }, onclick :+= ((e:Event) => {
                        open.set(!open.get)
                        if(open.get) {
                          widget.get.widget.afterRender()
                        }
                      })))
                    ),
                    tr(ClientConf.style.childTableTr,ClientConf.style.childFormTableTr,
                      produce(open) { o =>  if(!o) frag().render else
                        td(ClientConf.style.childFormTableTd, colspan := fields.length + 1,
                          div(
                            widget.get.widget.render(write, Property(true)),
                            if (write) div(
                              BootstrapStyles.Grid.row,
                              div(BootstrapCol.md(12), ClientConf.style.block,
                                div(BootstrapStyles.Float.right(),
                                  a(onclick :+= ((_: Event) => removeItem(e.get)), Labels.subform.remove)
                                )
                              )
                            ) else frag()
                          )
                        ).render
                      }
                    ).render

                  ).render
                })
              ),
              tr(ClientConf.style.childTableTr,
                td(ClientConf.style.childTableTd,colspan := fields.length + 1,
                  if (write) a(onclick :+= ((e: Event) => addItem(child, f)), Labels.subform.add) else frag()
                )
              )
            ).render,

          )

        }
      }
    }
  }


}

