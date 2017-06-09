package ch.wsl.box.client.services

import ch.wsl.box.client.utils.Session
import ch.wsl.box.model.shared._
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

  def fetchLookupOptions(values:Seq[Json],field:JSONField,opts:JSONFieldOptions):JSONField = {
    println(s"fetchLookups for field $field")
    val options:Map[String,String] = values.map{ value =>
      val key:String = value.get(opts.map.valueProperty)
      val label:String = value.get(opts.map.textProperty)
      (key,label)
    }.toMap
    field.copy(options = Some(field.options.get.copy(options = Map("" -> "") ++ options)))
  }

//  def fetchLookup(field:JSONField,opts:JSONFieldOptions,data:Seq[Json]):Future[JSONField] = {
//    println(s"fetchLookup for field $field")
//    val values = data.flatMap{row =>
//      row.get(field.key) match {
//        case "" => None
//        case s => Some{
//          JSONQueryFilter(opts.map.valueProperty, Some(Filter.EQUALS), s)
//        }
//      }
//    }
//
//    if(values.length > 0) {
//      val query = JSONQuery(
//        count = 1,
//        page = 1,
//        sort = List(),
//        filter = values.toList
//      )
//      REST.list("model", opts.refModel, query).map { values =>
//        val options: Map[String, String] = values.map { value =>
//          val key: String = value.get(opts.map.valueProperty)
//          val label: String = value.get(opts.map.textProperty)
//          (key, label)
//        }.toMap
//        field.copy(options = Some(field.options.get.copy(options = options)))
//      }
//    } else {
//      Future.successful(field)
//    }
//  }

  def fetchModels(forms:Seq[JSONMetadata]):Future[Seq[(String,Seq[Json])]] = {
    val models = for{
      form <- forms
      field <- form.fields
      option <- field.options
    } yield option.refModel
    Future.sequence {
      models.distinct.map { model: String =>
        println(s"fetching Model: $model")
        REST.list("model",Session.lang(), model, 6000).map(r => model -> r)
      }
    }
  }

  def populateOptionsValuesInFields(models:Seq[(String,Seq[Json])], form:JSONMetadata, data:Seq[Json]):JSONMetadata = {

      val fields = form.fields.map { field =>
        field.options match {
          //case Some(opts) if form.keys.contains(field.key) => fetchLookup(field, opts, data)
          case Some(opts) => {
            val data = models.find(_._1 == opts.refModel).map(_._2).getOrElse(Seq())
            fetchLookupOptions(data,field, opts)
          }
          case _ => field
        }
      }
      form.copy(fields = fields)

  }

  import io.circe.syntax._

  def parseOption(options:JSONFieldOptions,valueToSave:Json):Json = {
    options.options.find(_._2 == valueToSave.string)
      .map(x =>
        if(x._2 == "") return Json.Null else
          Try(x._1.toInt.asJson).getOrElse(x._1.asJson)
      ).getOrElse(Json.Null)
  }

  def parse(field: JSONField,value:Option[Json], keys:Seq[String])(onError:(Throwable => Unit)):(String,Json) = try{
    println(s"parsing ${field.key} with value $value")

    val valueToSave:Json = value match {
      case None => Json.Null
      case Some(v) => v
    }
    val data = (field.`type`,field.options) match {
      case (_,Some(options)) if !keys.contains(field.key) => parseOption(options,valueToSave)
      case (_,_) => valueToSave
    }

    (field.key,data)
  } catch { case t: Throwable =>
    onError(t)
    throw t
  }

  def extract(current:Json,form:JSONMetadata):Seq[(String,Json)] = form.fields.map{ field =>

    val value = current.js(field.key)

//    val result = field.options match {
//      case Some(opts) if !form.keys.contains(field.key) => {
//        val resultString = current.get(field.key)
//        opts.options.lift(resultString).getOrElse(resultString).asJson
//      }
//      case _ => value
//    }

    field.key -> value


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
