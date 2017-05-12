package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 03/03/16.
  */
case class JSONResult[M <: Product](count:Int,data:List[M]) {

  import JSONResult._
  def csv:String = data.map(_.toCSV()).mkString("\r\n")
}

object JSONResult{
  implicit class CSVWrapper(val prod: Product) extends AnyVal {

    private def escape(value:Any):String = {
      val str = value.toString
//      str.contains(",") || str.contains("\n")  match {
//        case true => "\""+str.replaceAll("\"","\\\"")+"\""
//        case false => str
//      }
      "\""+str.replaceAll("\"","\\\"")+"\""
    }

    def toCSV() = prod.productIterator.map{
      case Some(value) => escape(value)
      case None => escape("")
      case rest => escape(rest)
    }.mkString(",")
  }
}