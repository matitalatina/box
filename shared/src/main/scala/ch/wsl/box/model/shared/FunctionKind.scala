package ch.wsl.box.model.shared

object FunctionKind {
  object Modes {
    val TABLE = "table"
    val PDF = "pdf"
    val HTML = "html"
    val SHP = "shp"

    val all = Seq(TABLE,PDF,HTML,SHP)
  }
}
