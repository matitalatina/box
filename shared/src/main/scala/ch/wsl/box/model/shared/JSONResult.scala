package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 03/03/16.
  */
case class JSONResult[M <: Product](count:Int,data:List[M]) {

  import JSONResult._
  def csv:String = data.map(_.toCSV()).mkString("\n")
}

object JSONResult{
  implicit class CSVWrapper(val prod: Product) extends AnyVal {
    def toCSV() = prod.productIterator.map{
      case Some(value) => value
      case None => ""
      case rest => rest
    }.mkString(",")
  }
}