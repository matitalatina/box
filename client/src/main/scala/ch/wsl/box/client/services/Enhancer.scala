package ch.wsl.box.client.services

import ch.wsl.box.model.shared.{JSONField, JSONFieldOptions, JSONKeys}
import io.circe._
import io.circe.syntax._


import scala.concurrent.Future
import scala.util.Try

/**
  * Created by andre on 5/2/2017.
  */
object Enhancer {

  import ch.wsl.box.shared.utils.JsonUtils._

  import _root_.scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  def fetchLookupOptions(field:JSONField,opts:JSONFieldOptions):Future[JSONField] = {
    REST.list("model",opts.refModel,50).map{ values =>
      val options:Map[String,String] = values.map{ value =>
        val key:String = value.get(opts.map.valueProperty)
        val label:String = value.get(opts.map.textProperty)
        (key,label)
      }.toMap
      field.copy(options = Some(field.options.get.copy(options = Map("" -> "") ++ options)))
    }
  }

  def populateOptionsValuesInFields(fields:Seq[JSONField]):Future[Seq[JSONField]] = Future.sequence{
    fields.map{ field =>
      field.options match {
        case None => Future.successful(field)
        case Some(opts) => fetchLookupOptions(field,opts)
      }
    }
  }

  import io.circe.syntax._

  def parseOption(options:JSONFieldOptions,valueToSave:Json):Json = {
    options.options.find(_._2 == valueToSave.string)
      .map(x =>
        if(x._2 == "") return Json.Null else
          Try(x._1.toInt.asJson).getOrElse(x._1.asJson)
      ).getOrElse(Json.Null)
  }

  def parse(field: JSONField,value:Option[Json])(onError:(Throwable => Unit)):(String,Json) = try{
    println(s"parsing ${field.key} with value $value")

    val valueToSave:Json = value match {
      case None => Json.Null
      case Some(v) => v
    }
    val data = (field.`type`,field.options) match {
      case (_,Some(options)) => parseOption(options,valueToSave)
      case (_,_) => valueToSave
    }

    (field.key,data)
  } catch { case t: Throwable =>
    onError(t)
    throw t
  }

  def extract(current:Json,fields:Seq[JSONField]):Seq[Json] = fields.map{ field =>

    val result = current.hcursor.get[Json](field.key).right.getOrElse(Json.Null)

    field.options match {
      case None => result
      case Some(opts) => {
        val resultString = current.get(field.key)
        opts.options.lift(resultString).getOrElse(resultString).asJson
      }
    }

  }




  def extractKeys(row:Seq[String],fields:Seq[String],keys:Seq[String]):JSONKeys = {
    val map = for{
      key <- keys
      (field,i) <- fields.zipWithIndex if field == key
    } yield {
      key -> row.lift(i).getOrElse("")
    }
    JSONKeys.fromMap(map.toMap)
  }

}
