package ch.wsl.box.rest.logic

import akka.stream.scaladsl.Source
import io.circe._
import io.circe.syntax._
import ch.wsl.box.model.shared._
import ch.wsl.box.model.EntityActionsRegistry
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

case class FormActions(metadata:JSONMetadata)(implicit db:Database) extends UglyDBFilters {

  import ch.wsl.box.shared.utils.JsonUtils._


  val actions = EntityActionsRegistry.tableActions(metadata.entity)

  val jsonCustomMetadataFactory = JSONFormMetadataFactory()

  def getAllById(id:JSONID):Future[Json] = extractOne(id.query)

  def extractArray(query:JSONQuery):Future[Json] = extractSeq(query).map(_.asJson)     // todo adapt JSONQuery to select only fields in form
  def extractOne(query:JSONQuery):Future[Json] = extractSeq(query).map(x => if(x.length >1) throw new Exception("Multiple rows retrieved with single id") else x.headOption.asJson)

  def csv(query:JSONQuery):Future[String] = for {
    results <- extractSeq(query)
  } yield {
    val strings = results.map { row =>
      metadata.tabularFields.map { field =>
        row.get(field)
      }
    }
    CSV.of(strings)
  }

  def attachArrayIndex(jsonToInsert:Seq[Json],form:JSONMetadata):Seq[Json] = {
    jsonToInsert.zipWithIndex.map{ case (jsonRow,i) =>
      val values = form.fields.filter(_.default.contains("arrayIndex")).map{ fieldToAdd =>
        fieldToAdd.name -> i
      }.toMap
      jsonRow.deepMerge(values.asJson) //overwrite field value with array index
    }
  }

  def deleteChild(child:JSONMetadata, receivedJson:Seq[Json], dbJson:Seq[Json]) = {
    val receivedID = receivedJson.map(_.ID(child.keys))
    val dbID = dbJson.map(_.ID(child.keys))
    println(s"child: ${child.name} received: ${receivedID.map(_.asString)} db: ${dbID.map(_.asString)}")
    dbID.filterNot(k => receivedID.contains(k)).map{ idsToDelete =>
      println(s"Deleting child ${child.name}, with key: $idsToDelete")
      EntityActionsRegistry.tableActions(child.entity).delete(idsToDelete)
    }
  }

  def updateAll(e:Json):Future[Json] = {

    def submetadata = metadata.fields.filter(_.child.isDefined).map { field =>
      for {
        form <- jsonCustomMetadataFactory.of(field.child.get.objId, metadata.lang)
        dbSubforms <- getChild(e,field,form,field.child.get)
        subJson = attachArrayIndex(e.seq(field.name),form)
        deleted = deleteChild(form,subJson,dbSubforms)
        result <- FutureUtils.seqFutures(subJson){ json => //order matters so we do it synchro
            FormActions(form).updateAll(json).recover{case t => t.printStackTrace(); Json.Null}
        }
      } yield result
    }
    val id = e.ID(metadata.keys)
    for{
      _ <- Future.sequence(submetadata)
      dbData <- actions.getById(id).recover{ case t => println("recovered future with none"); None }   //existing record in db
      result <- {
        if(dbData.isDefined) {
          println(s"update $id")
          actions.update(id,e)
        } else {
          println(s"insert into ${metadata.entity} with id $id")
          actions.insert(e)
        }
      }
    } yield result

  }

  def insertAll(e:Json):Future[Json] = for{
    _ <- Future.sequence(metadata.fields.filter(_.child.isDefined).map { field =>
      for {
        metadata <- jsonCustomMetadataFactory.of(field.child.get.objId, metadata.lang)
        rows = attachArrayIndex(e.seq(field.name),metadata)
        result <- FutureUtils.seqFutures(rows)(row => FormActions(metadata).insertAll(row))
      } yield result
    })
    inserted <- actions.insert(e)
    data <- getAllById(inserted.ID(metadata.keys))
  } yield data


  private def createQuery(entity:Json, child: Child):JSONQuery = {
    val parentFilter = for{
      (local,remote) <- child.masterFields.split(",").zip(child.childFields.split(","))
    } yield {
      JSONQueryFilter(remote,Some(Filter.EQUALS),entity.get(local))
    }

    val filters = parentFilter.toSeq ++ child.childFilter

    JSONQuery.empty.copy(filter=filters.toList.distinct)
  }

  private def getChild(dataJson:Json, field:JSONField, metadata:JSONMetadata, child:Child):Future[Seq[Json]] = {
    val query = createQuery(dataJson,child)

    for {
      data <- FormActions(metadata).extractSeq(query)
    } yield data

  }

  private def expandJson(dataJson:Json):Future[Json] = {

    val values = metadata.fields.map{ field =>
      {(field.`type`,field.child) match {
        case ("static",_) => Future.successful(field.name -> field.default.asJson)  //set default value
        case (_,None) => Future.successful(field.name -> dataJson.js(field.name))        //use given value
        case (_,Some(child)) => for{
          form <- jsonCustomMetadataFactory.of(child.objId,metadata.lang)
          data <- getChild(dataJson,field,form,child)
        } yield {
          println(s"expanding child ${field.name} : ${data.asJson}")
          field.name -> data.asJson
        }
      }}.recover{ case t =>
        t.printStackTrace()
        field.name -> Json.Null
      }
    }
    Future.sequence(values).map(_.toMap.asJson)
  }

  private def extractSeq(query:JSONQuery):Future[Seq[Json]] = {
    for{
      data <- EntityActionsRegistry.tableActions(metadata.entity).getEntity(query)
      expandedData <- Future.sequence(data.map(expandJson))
    } yield expandedData
  }.recover{ case t =>
    t.printStackTrace()
    Seq()
  }

}
