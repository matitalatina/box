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
    "\""+str.replaceAll("\"","\\\"").replaceAll("\n","\\\\n")+"\""
  }

  def of(data:Seq[Seq[String]]):String = data.map(row).mkString("\n")

  def row(row:Seq[String]):String = row.map(escape).mkString(",")

  def split(data:String):Seq[Seq[String]] = data.split("\n").toSeq.map{x => x.replaceAll("\\\\n","\n").substring(1,x.length-1).split("\",\"").toSeq}
}
