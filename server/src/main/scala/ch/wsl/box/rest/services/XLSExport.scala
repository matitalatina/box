package ch.wsl.box.rest.services

import java.io.OutputStream

import com.norbitltd.spoiwo.model.enums.CellFill
import com.norbitltd.spoiwo.model.{CellStyle, Color, Font, Row, Sheet}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._

case class XLSTable(title: String,header:Seq[String],rows:Seq[Seq[String]])

object XLSExport {

  private val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

  def apply(table:XLSTable,stream:OutputStream): Unit = {
    val helloWorldSheet = Sheet(name = table.title,
      rows = (Seq(Row(style = headerStyle).withCellValues(table.header.toList)) ++ table.rows.map(r => Row().withCellValues(r.toList))).toList
    )
    helloWorldSheet.writeToOutputStream(stream)
  }
}
