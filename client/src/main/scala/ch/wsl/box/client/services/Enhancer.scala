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

  def fetchLookup(values:Seq[Json], field:JSONField, opts:JSONFieldLookup):JSONField = {
    println(s"fetchLookups for field $field")
    val options:Map[String,String] = values.map{ value =>
      val key:String = value.get(opts.map.valueProperty)
      val label:String = value.get(opts.map.textProperty)
      (key,label)
    }.toMap
    field.copy(lookup = Some(field.lookup.get.copy(lookup = Map("" -> "") ++ options)))
  }


  def fetchLookupEntities(forms:Seq[JSONMetadata]):Future[Seq[(String,Seq[Json])]] = {
    val models = for{
      form <- forms
      field <- form.fields
      lookup <- field.lookup   //fields without lookup are an empty list
    } yield lookup.lookupEntity

    Future.sequence {
      models.distinct.map { model: String =>
        println(s"fetching Model: $model")
        REST.list("entity", Session.lang(), model, 6000).map(r => model -> r)
      }
    }
  }

  def populateLookupValuesInFields(models:Seq[(String,Seq[Json])], form:JSONMetadata):JSONMetadata = {

      val fields = form.fields.map { field =>
        field.lookup match {
          //case Some(opts) if form.keys.contains(field.key) => fetchLookup(field, opts, data)
          case Some(opts) => {
            val data = models.find(_._1 == opts.lookupEntity).map(_._2).getOrElse(Seq())
            fetchLookup(data, field, opts)
          }
          case _ => field
        }
      }
      form.copy(fields = fields)

  }

  import io.circe.syntax._



  def parse(field: JSONField,value:Option[Json])(onError:(Throwable => Unit)):(String,Json) = try{
    println(s"parsing ${field.name} with value $value")

    val valueToSave:Json = value match {
      case None => Json.Null
      case Some(v) => v
    }

    (field.name,valueToSave)
  } catch { case t: Throwable =>
    onError(t)
    throw t
  }

  def extract(current:Json,form:JSONMetadata):Seq[(String,Json)] = form.fields.map{ field =>

    val value = current.js(field.name)

    field.name -> value
  }




  def extractIDs(row:Seq[String], fields:Seq[String], keys:Seq[String]):JSONIDs = {
    val map = for{
      key <- keys
      (field,i) <- fields.zipWithIndex if field == key
    } yield {
      key -> row.lift(i).getOrElse("")
    }
    JSONIDs.fromMap(map.toMap)
  }

}
