package ch.wsl.box.client.views.components

import java.util.UUID

import ch.wsl.box.client.services.{ClientConf, ClientSession, Labels, REST}
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles, Icons}
import ch.wsl.box.client.utils.TestHooks
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
import ch.wsl.box.client.Context._

/**
  * Created by andre on 6/1/2017.
  */

case class ChildRow(widget:ChildWidget,id:String, data:ReadableProperty[Json], open:Boolean, rowId:Option[JSONID])

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

    def row_id: Property[Option[String]]
    def prop: Property[Json]
    def field:JSONField

    val childWidgets: scala.collection.mutable.ListBuffer[ChildRow] = scala.collection.mutable.ListBuffer()
    val entity: SeqProperty[String] = SeqProperty(Seq())
    val metadata = children.find(_.objId == child.objId)

    protected def render(write: Boolean): JsDom.all.Modifier

    private def add(data:Json,open:Boolean): Unit = {

      val id = UUID.randomUUID().toString
      val propData = Property(data)
      val childId = Property(data.ID(metadata.get.keys).map(_.asString))

      propData.listen{data =>
        val newData = prop.get.as[Seq[Json]].toSeq.flatten.map{x =>
          if(x.ID(metadata.get.keys) == data.ID(metadata.get.keys)) {
            x.deepMerge(data)
          } else x
        }
        propListener.cancel()
        prop.set(newData.asJson)
        registerListener(false)
      }

      val widget = JSONMetadataRenderer(metadata.get, propData, children, childId)

      val childRow = ChildRow(widget,id,propData,open,JSONID.fromData(propData.get,metadata.get))
      childWidgets += childRow
      entity.append(id)
      logger.debug(s"Added row ${childRow.rowId.map(_.asString).getOrElse("No ID")} of childForm ${metadata.get.name}")
      widget.afterRender()
    }

    def splitJson(js: Json): Seq[Json] = {
      js.as[Seq[Json]].right.getOrElse(Seq()) //.map{ js => js.deepMerge(Json.obj((subformInjectedId,Random.alphanumeric.take(16).mkString.asJson)))}
    }


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

        val oldData = cw.widget.data.get
        val newData = rows.lift(i).getOrElse(Json.obj())

        logger.debug(s"olddata: $oldData")
        logger.debug(s"newdata: $newData")

        val d = oldData.deepMerge(newData)

        logger.debug(s"result: $newData")

        f(cw.widget)(d, childMetadata).map{ r =>
          r
        }
      }.toSeq)
      //correct futures
      out
    }

    def collectData(jsChilds:Seq[Json]) = {
      logger.debug(Map(child.key -> jsChilds.asJson).asJson.toString())
      Map(child.key -> jsChilds.asJson).asJson
    }

    override def afterSave(data: Json, m: JSONMetadata): Future[Json] = {
      logger.info(data.toString())
      metadata.foreach { met =>
        //Set new inserted records open by default
        val oldData: Seq[JSONID] = this.prop.get.as[Seq[Json]].getOrElse(Seq()).flatMap(x => JSONID.fromData(x, met))
        val newData: Seq[JSONID] = data.seq(field.name).flatMap(x => JSONID.fromData(x, met))

        newData.foreach{ id =>
          if(!oldData.contains(id)) {
            services.clientSession.setTableChildOpen(ClientSession.TableChildElement(field.name,met.objId,Some(id)))
          }
        }

      }


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



    var propListener:Registration = null

    def registerListener(immediate:Boolean) {
      propListener = prop.listen(i => {
        childWidgets.foreach(_.widget.killWidget())
        childWidgets.clear()
        entity.clear()
        val entityData = splitJson(prop.get)

        entityData.foreach { x =>
          val isOpen:Boolean = services.clientSession.isTableChildOpen(ClientSession.TableChildElement(
            field.name,
            metadata.map(_.objId).getOrElse(-1),
            metadata.flatMap(m => JSONID.fromData(x,m))
          ))
          add(x, isOpen)
        }
      }, immediate)
    }

    registerListener(true)




  }


}

case class SimpleChildFactory(child:Child, children:Seq[JSONMetadata], masterData:Property[Json]) extends ChildRendererFactory {
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = SimpleChildRenderer(id,prop,field)

  case class SimpleChildRenderer(row_id: Property[Option[String]], prop: Property[Json], field:JSONField) extends ChildRenderer {

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
  override def create(id: _root_.io.udash.Property[Option[String]], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = TableChildRenderer(id,prop,field)

  case class TableChildRenderer(row_id: Property[Option[String]], prop: Property[Json], field:JSONField) extends ChildRenderer {

    import scalatags.JsDom.all._
    import io.udash.css.CssView._
    import ch.wsl.box.shared.utils.JSONUtils._

    override protected def render(write: Boolean): JsDom.all.Modifier = {

      metadata match {
        case None => p("child not found")
        case Some(f) => {

          val fields = f.rawTabularFields.flatMap{fieldId => f.fields.find(_.name == fieldId)}

          div(
            table(id := TestHooks.tableChildId(f.objId),ClientConf.style.childTable,
              tr(ClientConf.style.childTableTr,ClientConf.style.childTableHeader,
                td(),
                fields.map(f => td(ClientConf.style.childTableTd,f.title))
              ),
              tbody(
                autoRelease(produce(entity) { ent => //cannot use repeat because we have two childs for each iteration so frag is not working
                  ent.map { e =>
                    val widget = childWidgets.find(_.id == e)
                    val open = Property(widget.get.open)

                    def toggleRow() = {
                      val tableChildElement = ClientSession.TableChildElement(field.name,f.objId,widget.get.rowId)
                      open.set(!open.get)
                      if (open.get) {
                        services.clientSession.setTableChildOpen(tableChildElement)
                        widget.get.widget.afterRender()
                      } else {
                        services.clientSession.setTableChildClose(tableChildElement)
                      }
                    }

                    frag(
                      tr(`class` := TestHooks.tableChildRow,ClientConf.style.childTableTr,
                        td(ClientConf.style.childTableTd, ClientConf.style.childTableAction, a(id := TestHooks.tableChildButtonId(f.objId,widget.get.rowId), produce(open) {
                          case true => span(Icons.caretDown).render
                          case false => span(Icons.caretRight).render
                        }, onclick :+= ((e: Event) => toggleRow()))),
                        autoRelease(produce(widget.get.data) { data => fields.map(x => td(ClientConf.style.childTableTd, data.get(x.name))).render }),
                      ),
                      tr(ClientConf.style.childTableTr, ClientConf.style.childFormTableTr, id := TestHooks.tableChildRowId(f.objId,widget.get.rowId),
                        autoRelease(produce(open) { o =>
                          if (!o) frag().render else
                            td(ClientConf.style.childFormTableTd, colspan := fields.length + 1,
                              div(
                                widget.get.widget.render(write, Property(true)),
                                if (write) div(
                                  BootstrapStyles.Grid.row,
                                  div(BootstrapCol.md(12), ClientConf.style.block,
                                    div(BootstrapStyles.Float.right(),
                                      a(onclick :+= ((_: Event) => removeItem(e)), Labels.subform.remove)
                                    )
                                  )
                                ) else frag()
                              )
                            ).render
                        })
                      ).render

                    ).render
                  }
                })
              ),
              tr(ClientConf.style.childTableTr,
                td(ClientConf.style.childTableTd,colspan := fields.length + 1,
                  if (write) a(id := TestHooks.addChildId(f.objId),onclick :+= ((e: Event) => {
                    addItem(child, f)
                    true
                  }), Labels.subform.add) else frag()
                )
              )
            ).render,

          )

        }
      }
    }
  }


}

