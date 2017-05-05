package ch.wsl.box.client.services

import ch.wsl.box.model.shared.{JSONField, JSONFieldOptions, JSONKeys}
import io.circe.Json

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by andre on 5/2/2017.
  */
object Enhancer {

  import scalajs.concurrent.JSExecutionContext.Implicits.queue

  def fetchLookupOptions(field:JSONField,opts:JSONFieldOptions):Future[JSONField] = {
    REST.list(opts.refModel).map{ values =>
      val options:Map[String,String] = values.map{ value =>
        val key:String = value.hcursor.get[Json](opts.map.valueProperty).fold({x => println(x); ""},{x => x.toString})
        val label:String = value.hcursor.get[Json](opts.map.textProperty).fold({x => println(x); ""},{x => x.as[String].right.getOrElse(x.toString())})
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

  def parseOption(options:JSONFieldOptions,valueToSave:Option[String]):Json = {
    options.options.find(_._2 == valueToSave.getOrElse(""))
      .map(x =>
        if(x._2 == "") return Json.Null else
          Try(x._1.toInt.asJson).getOrElse(x._1.asJson)
      ).getOrElse(Json.Null)
  }

  def parse(field: JSONField,value:Option[String])(onError:(Throwable => Unit)):(String,Json) = try{
    println(s"parsing ${field.key} with value $value")

    val valueToSave = value match {
      case Some("") => None
      case _ => value
    }
    val data = (field.`type`,field.options) match {
      case (_,Some(options)) => parseOption(options,valueToSave)
      case ("string",_) => valueToSave.asJson
      case ("number",_) => valueToSave.map( v => v.toDouble).asJson
      case (_,_) => valueToSave.asJson
    }

    (field.key,data)
  } catch { case t: Throwable =>
    onError(t)
    throw t
  }

  def extract(current:Json,fields:Seq[JSONField]):Seq[String] = fields.map{ field =>
    val resultString = current.hcursor.get[Json](field.key).fold(
      _ => "",
      rawJs => rawJs.as[String].fold(
        _ => rawJs.toString(),
        x => x
      )
    )

    field.options match {
      case None => resultString
      case Some(opts) => opts.options.lift(resultString).getOrElse(resultString)
    }

  }


  implicit class EnhancedJson(el:Json) {
    def get(field: String) = el.hcursor.get[Json](field).fold(
      { x => println(x); "" }, { x => x.as[String].right.getOrElse(x.toString()) }
    )

    def keys(fields:Seq[String]) :JSONKeys = {
      val values = fields map { field =>
        get(field)
      }
      JSONKeys.fromMap(fields.zip(values).toMap)
    }
  }

}
