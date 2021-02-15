package ch.wsl.box.client.views.components.widget.child

import ch.wsl.box.client.Context.services
import ch.wsl.box.client.services.{ClientConf, ClientSession, Labels}
import ch.wsl.box.client.styles.constants.StyleConstants.Colors
import ch.wsl.box.client.styles.utils.ColorUtils
import ch.wsl.box.client.styles.{BootstrapCol, Icons, StyleConf}
import ch.wsl.box.client.utils.TestHooks
import ch.wsl.box.client.views.components.widget.{Widget, WidgetParams}
import ch.wsl.box.model.shared.{Child, JSONField, JSONMetadata, WidgetsNames}
import io.circe.Json
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.table.UdashTable
import io.udash.{Property, produce}
import org.scalajs.dom.{Event, document}
import scalacss.internal.mutable.StyleSheet
import scalacss.ScalatagsCss._
import scalacss.ProdDefaults._

case class TableStyle(conf:StyleConf) extends StyleSheet.Inline {
  import dsl._

  val lightMain = ColorUtils.RGB.fromHex(conf.colors.main.value).lighten(0.7).copy(saturation = 0.5).color

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
    borderWidth(1 px),
    borderColor(Colors.GreySemi),
    borderStyle.solid,
    padding.`0`,
    textAlign.left,
    fontSize(12 px)
  )

  val tr = style(
    borderWidth(0 px, 2 px, 1 px, 2 px),
    borderColor(Colors.GreySemi),
    borderStyle.solid,
  )

  val th = style(
    borderWidth(2 px),
    borderColor(conf.colors.main),
    borderStyle.solid,
    padding(5 px),
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



    import ch.wsl.box.shared.utils.JSONUtils._
    import io.udash.css.CssView._
    import scalatags.JsDom.all._

    override def child: Child = field.child.get

    override protected def render(write: Boolean): Modifier = {


      val columns = 10
      val rows = 100

      val tableStyle = TableStyle(ClientConf.styleConf)
      val tableStyleElement = document.createElement("style")
      tableStyleElement.innerText = tableStyle.render(cssStringRenderer,cssEnv)

      metadata match {
        case None => p("child not found")
        case Some(f) => {

          frag(
            tableStyleElement,
            div(tableStyle.tableContainer,
              table(tableStyle.table,
                thead(
                  for(i <- 1 to columns) yield {
                    th(s"longer col header$i", tableStyle.th)
                  }
                ),
                tbody(
                  for(j <- 1 to rows) yield {
                    tr(tableStyle.tr,
                      for (i <- 1 to columns) yield {
                        td(s"col slakj fhsalfkdj haskldfj shalkfdja h$i", tableStyle.td)
                      }
                    )
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