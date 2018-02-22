package ch.wsl.box.shared.utils


/**
  * Created by andre on 5/22/2017.
  */
object CSV {

  private def escape(str:String):String = {
    //      str.contains(",") || str.contains("\n")  match {
    //        case true => "\""+str.replaceAll("\"","\\\"")+"\""
    //        case false => str
    //      }
    "\""+str.replaceAll("\"","\\\"")+"\""
  }

  def of(data:Seq[Seq[String]]):String = data.map(row).mkString("\r\n")

  def row(row:Seq[String]):String = row.map(escape).mkString(",")
}
