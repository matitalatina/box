package ch.wsl.box.rest.logic

import akka.stream.scaladsl.Source
import io.circe._
import io.circe.syntax._
import ch.wsl.box.model.shared._
import ch.wsl.box.model.TablesRegistry
import ch.wsl.box.rest.utils.FutureUtils
import ch.wsl.box.shared.utils.CSV
import io.circe.Json
import slick.lifted.Query
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andre on 5/18/2017.
  *
  * translate db data to JSONForm structure
  */

case class ReferenceKey(localField:String,remoteField:String,value:String)
case class Reference(association:Seq[ReferenceKey])

case class FormShaper(form:JSONMetadata)(implicit db:Database) extends UglyDBFilters {

  import ch.wsl.box.shared.utils.JsonUtils._


  val actions = TablesRegistry.actions(form.table)
  def jsonFormMetadata = JSONFormMetadata()

  private def createQuery(model:Json, subform: Subform):JSONQuery = {
    val parentFilter = for{
      (local,remote) <- subform.localFields.split(",").zip(subform.subFields.split(","))
    } yield {
      JSONQueryFilter(remote,Some(Filter.EQUALS),model.get(local))
    }

    val filters = parentFilter.toSeq ++ subform.subFilter

    JSONQuery(None,List(),filters.toList.distinct)
  }

  private def getSubform(model:Json, field:JSONField, form:JSONMetadata, subform:Subform):Future[Seq[Json]] = {
    val query = createQuery(model,subform)

    for {
      result <- FormShaper(form).extractSeq(query)
    } yield result

  }



  private def expandJson(json:Json):Future[Json] = {

    val values = form.fields.map{ field =>
      {(field.`type`,field.subform) match {
        case ("static",_) => Future.successful(field.key -> field.default.asJson)  //set default value
        case (_,None) => Future.successful(field.key -> json.js(field.key))        //use given value
        case (_,Some(subform)) => for{
          form <- jsonFormMetadata.get(subform.id,form.lang)
          result <- getSubform(json,field,form,subform)
        } yield field.key -> result.asJson
      }}.recover{ case t =>
        t.printStackTrace()
        field.key -> Json.Null
      }
    }
    Future.sequence(values).map(_.toMap.asJson)
  }

  private def extractSeq(query:JSONQuery):Future[Seq[Json]] = {
    for{
      elements <- TablesRegistry.actions(form.table).getModel(query)
      result <- Future.sequence(elements.map(expandJson))
    } yield result
  }.recover{ case t =>
    t.printStackTrace()
    Seq()
  }

  def getAllById(key:JSONKeys) = extractOne(key.query)

  def extractArray(query:JSONQuery):Future[Json] = extractSeq(query).map(_.asJson)     // todo adapt JSONQuery to select only fields in form
  def extractOne(query:JSONQuery):Future[Json] = extractSeq(query).map(x => if(x.length >1) throw new Exception("Multiple rows retrieved with single key") else x.headOption.asJson)

  def csv(query:JSONQuery):Future[String] = for {
    results <- extractSeq(query)
  } yield {
    val strings = results.map { row =>
      form.tableFields.map { field =>
        row.get(field)
      }
    }
    CSV.of(strings)
  }

  def attachArrayIndex(jsonToInsert:Seq[Json],form:JSONMetadata):Seq[Json] = {
    jsonToInsert.zipWithIndex.map{ case (jsonRow,i) =>
      val values = form.fields.filter(_.default.contains("arrayIndex")).map{ fieldToAdd =>
        fieldToAdd.key -> i
      }.toMap
      jsonRow.deepMerge(values.asJson) //overwrite field value with array index
    }
  }

  def deleteSubforms(subform:JSONMetadata, recivedJson:Seq[Json],dbJson:Seq[Json]) = {
    val receivedKeys = recivedJson.map(_.keys(subform.keys))
    val dbKeys = dbJson.map(_.keys(subform.keys))
    println(s"subform: ${subform.name} received: ${receivedKeys.map(_.asString)} db: ${dbKeys.map(_.asString)}")
    dbKeys.filterNot(k => receivedKeys.contains(k)).map{ keysToDelete =>
      println(s"Deleting subform ${subform.name}, with key: $keysToDelete")
      TablesRegistry.actions(subform.table).delete(keysToDelete)
    }
  }

  def updateAll(e:Json):Future[Json] = {

    def subforms = form.fields.filter(_.subform.isDefined).map { field =>
      for {
        form <- jsonFormMetadata.get(field.subform.get.id, form.lang)
        dbSubforms <- getSubform(e,field,form,field.subform.get)
        subJson = attachArrayIndex(e.seq(field.key),form)
        deleted = deleteSubforms(form,subJson,dbSubforms)
        result <- FutureUtils.seqFutures(subJson){ json => //order matters so we do it synchro
            FormShaper(form).updateAll(json).recover{case t => t.printStackTrace(); Json.Null}
        }
      } yield result
    }
    val key = e.keys(form.keys)
    for{
      _ <- Future.sequence(subforms)
      existing <- actions.getById(key).recover{ case t => println("recovered future with none"); None }   //existing record in db
      result <- {
        if(existing.isDefined) {
          println(s"update $key")
          actions.update(key,e)
        } else {
          println(s"insert into ${form.table} with key $key")
          actions.insert(e)
        }
      }
    } yield result

  }

  def insertAll(e:Json):Future[Json] = for{
    _ <- Future.sequence(form.fields.filter(_.subform.isDefined).map { field =>
      for {
        form <- jsonFormMetadata.get(field.subform.get.id, form.lang)
        rows = attachArrayIndex(e.seq(field.key),form)
        result <- FutureUtils.seqFutures(rows)(row => FormShaper(form).insertAll(row))
      } yield result
    })
    inserted <- actions.insert(e)
    result <- getAllById(inserted.keys(form.keys))
  } yield result

}
