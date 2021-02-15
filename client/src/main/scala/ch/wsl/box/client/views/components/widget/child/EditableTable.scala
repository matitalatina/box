package ch.wsl.box.client.views.components.widget.child

import ch.wsl.box.client.Context.services
import ch.wsl.box.client.services.{BrowserConsole, ClientConf, ClientSession, Labels}
import ch.wsl.box.client.styles.constants.StyleConstants.Colors
import ch.wsl.box.client.styles.utils.ColorUtils
import ch.wsl.box.client.styles.{BootstrapCol, Icons, StyleConf}
import ch.wsl.box.client.utils.TestHooks
import ch.wsl.box.client.views.components.widget.{Widget, WidgetParams, WidgetRegistry}
import ch.wsl.box.model.shared.{Child, JSONField, JSONMetadata, WidgetsNames}
import io.circe._
import io.circe.syntax._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.table.UdashTable
import io.udash._
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLElement
import scalacss.internal.mutable.StyleSheet
import scalacss.ScalatagsCss._
import scalacss.ProdDefaults._

case class TableStyle(conf:StyleConf,columns:Int) extends StyleSheet.Inline {
  import dsl._

  val selectedBorder = 2
  val cellPadding = 4

  private val lightMain = ColorUtils.RGB.fromHex(conf.colors.main.value).lighten(0.7).copy(saturation = 0.5).color

  val tableContainer = style(
    margin(10 px),
    overflow.auto
  )

  val table = style(
    margin(2 px),
    borderColor(Colors.GreySemi),
    borderCollapse.collapse,
    minWidth(100.%%)
  )

  val td = style(
    width((100/columns).%%),
    boxSizing.borderBox,
    borderWidth(1 px),
    borderColor(Colors.GreySemi),
    borderStyle.solid,
    padding(cellPadding px),
    textAlign.left,
    fontSize(12 px),
    cursor.pointer
  )

  val selectedWrapper = style(
    overflow.visible,
    backgroundColor(conf.colors.main),
    padding(selectedBorder px),
    margin(-cellPadding px)

  )

  val selectedContent = style(
    padding((cellPadding-selectedBorder) px),
    backgroundColor.white
  )


  val tr = style(
    borderWidth(0 px, 2 px, 1 px, 2 px),
    borderColor(Colors.GreySemi),
    borderStyle.solid,
  )



  val th = style(
    width((100/columns).%%),
    borderWidth(2 px),
    borderColor(conf.colors.main),
    borderStyle.solid,
    padding(cellPadding px),
    whiteSpace.nowrap,
    backgroundColor(conf.colors.main),
    color(conf.colors.mainText),
    fontSize(14 px),
    fontWeight.bold
  )

}

object EditableTable extends ChildRendererFactory {


  override def name: String = WidgetsNames.editableTable


  override def create(params: WidgetParams): Widget = EditableTableRenderer(params.id,params.prop,params.field,params.allData,params.children)


  case class EditableTableRenderer(row_id: Property[Option[String]], prop: Property[Json], field:JSONField,masterData:Property[Json],children:Seq[JSONMetadata]) extends ChildRenderer {


    val tableStyle = TableStyle(ClientConf.styleConf,metadata.map(_.fields.length).getOrElse(1))
    val tableStyleElement = document.createElement("style")
    tableStyleElement.innerText = tableStyle.render(cssStringRenderer,cssEnv)

    import ch.wsl.box.shared.utils.JSONUtils._
    import io.udash.css.CssView._
    import scalatags.JsDom.all._

    override def child: Child = field.child.get

    private val selectedCellClass = "_et_selected"

    case class Cell(id:String,widget:Widget,onChange: () => Unit)

    private var cell:Option[Cell] = None

    def resetTable() = {
      val editingElements = document.getElementsByClassName(selectedCellClass)
      for(i <- 0 until editingElements.length) {
        cell.foreach(_.widget.killWidget())
        editingElements.item(i).innerHTML = div(cell.toSeq.map(_.widget.showOnTable())).render.innerHTML
        editingElements.item(i).classList.remove(selectedCellClass)
      }
    }

    def selectCell(td: HTMLElement,cell:Cell): Unit = {

      if(this.cell.isEmpty) {
        this.cell = Some(cell)
        td.classList.add(selectedCellClass)
      }

      if(this.cell.forall(_.id != cell.id)) {
        resetTable()
        this.cell.get.onChange()
        td.classList.add(selectedCellClass)
        this.cell = Some(cell)
      }

      cell.widget.killWidget()

      val el = div(
        tableStyle.selectedWrapper,
        height := td.clientHeight,
        width := td.clientWidth,
        div(
          tableStyle.selectedContent,
          height := td.clientHeight-(tableStyle.selectedBorder*2),
          width := td.clientWidth-(tableStyle.selectedBorder*2),
          cell.widget.editOnTable()
        )
      ).render
      td.innerHTML = ""
      td.appendChild(el)

      td.querySelector("input").asInstanceOf[HTMLElement].focus()

    }


    override protected def render(write: Boolean): Modifier = {


      val columns = 10
      val rows = 100



      metadata match {
        case None => p("child not found")
        case Some(f) => {

          frag(
            tableStyleElement,
            div(tableStyle.tableContainer,
              table(tableStyle.table,
                thead(
                  for(field <- f.fields) yield {
                    val name = field.label.getOrElse(field.name)
                    th(name, tableStyle.th)
                  }
                ),
                tbody(
                  produce(entity) { ent =>
                    for(row <- ent) yield {
                      val childWidget = childWidgets.find(_.id == row).get
                      tr(tableStyle.tr,
                        for (field <- f.fields) yield {
                          val widgetFactory = field.widget.map(WidgetRegistry.forName).getOrElse(WidgetRegistry.forType(field.`type`))
                          val data:Property[Json] = Property(Json.Null)
                          var listener = childWidget.widget.data.listen(d => data.set(d.js(field.name)),true)
                          val widget = widgetFactory.create(WidgetParams(
                            id = Property(childWidget.rowId.map(_.asString)),
                            prop = data,
                            field = field, metadata = f, allData = childWidget.widget.data, children = Seq()
                          ))
                          def change() = {
                            val newData = childWidget.widget.data.get.deepMerge(Json.obj(field.name -> data.get))
                            listener.cancel()
                            childWidget.widget.data.set(newData)
                            listener = childWidget.widget.data.listen(d => data.set(d.js(field.name)))
                          }
                          td(widget.showOnTable(), tableStyle.td,
                            onclick :+= ((e:Event) => selectCell(e.target.asInstanceOf[HTMLElement],Cell(row+field.name,widget,change)))
                          )
                        }
                      ).render
                    }
                  }
                )
              )
            )
          )



        }
      }
    }
  }


}