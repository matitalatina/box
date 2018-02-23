package ch.wsl.box.model.shared

import ch.wsl.box.shared.utils.CSV

/**
  * Created by andreaminetti on 03/03/16.
  */
case class JSONData[M <: Product](data:List[M], count:Int) {

  import JSONData._
  def csv:String = CSV.of(data.map(_.values()))
}

object JSONData{
  implicit class CSVWrapper(val prod: Product) extends AnyVal {

    def values():Seq[String] = prod.productIterator.map{
      case Some(value) => value.toString
      case None => ""
      case rest => rest.toString
    }.toSeq
  }
}