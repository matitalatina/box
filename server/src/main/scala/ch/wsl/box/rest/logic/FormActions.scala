package ch.wsl.box.rest.logic

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.circe._
import io.circe.syntax._
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.routes.enablers.CSVDownload
import ch.wsl.box.rest.utils.{FutureUtils, Timer, UserProfile}
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}
import io.circe.Json
import scribe.Logging
import slick.basic.DatabasePublisher
import slick.lifted.Query
import ch.wsl.box.rest.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.MetadataFactory

import scala.concurrent.{ExecutionContext, Future}


case class ReferenceKey(localField:String,remoteField:String,value:String)
case class Reference(association:Seq[ReferenceKey])

case class FormActions(metadata:JSONMetadata,
                       jsonActions: String => EntityJSONTableActions,
                       metadataFactory: MetadataFactory
                      )(implicit db:Database, mat:Materializer, ec:ExecutionContext) extends UglyDBFilters with Logging {

  import ch.wsl.box.shared.utils.JSONUtils._



  val jsonAction = jsonActions(metadata.entity)


  def getById(id:JSONID):Future[Json] = {
    logger.info("Getting Form data")
    get(id.query)
  }

  private def streamSeq(query:JSONQuery):Source[Json,NotUsed] = {
    Source
      .fromPublisher(jsonAction.findStreamed(query))
      .flatMapConcat( json => Source.fromFuture(expandJson(json)))
  }
  def streamArray(query:JSONQuery):Source[Json,NotUsed] = streamSeq(query)
  def get(query:JSONQuery):Future[Json] = streamSeq(query).runFold(Seq[Json]())(_ ++ Seq(_)).map(x =>
      if(x.length >1){
        throw new Exception("Multiple rows retrieved with single id")
      } else {
        x.headOption.asJson
      })

  def csv(query:JSONQuery,lookupElements:Option[Map[String,Seq[Json]]],fields:JSONMetadata => Seq[String] = _.tabularFields):Source[String,NotUsed] = {

      val lookup = Lookup.valueExtractor(lookupElements, metadata) _

      streamSeq(query).map { json =>
        val row = fields(metadata).map { field =>
          lookup(field,json.get(field))
        }
        CSV.writeRow(row)
      }

  }

  def attachArrayIndex(jsonToInsert:Seq[Json],form:JSONMetadata):Seq[Json] = {
    jsonToInsert.zipWithIndex.map{ case (jsonRow,i) =>
      val values = form.fields.filter(_.default.contains("arrayIndex")).map{ fieldToAdd =>
        fieldToAdd.name -> i
      }.toMap
      jsonRow.deepMerge(values.asJson) //overwrite field value with array index
    }
  }

  def subAction[T](e:Json, nullElement:T,action: FormActions => (Json => Future[T])): Seq[Future[List[T]]] = metadata.fields.filter(_.child.isDefined).map { field =>
    for {
      form <- metadataFactory.of(field.child.get.objId, metadata.lang)
      dbSubforms <- getChild(e,field,form,field.child.get)
      subJson = attachArrayIndex(e.seq(field.name),form)
      deleted = deleteChild(form,subJson,dbSubforms)
      result <- FutureUtils.seqFutures(subJson){ json => //order matters so we do it synchro
        action(FormActions(form,jsonActions,metadataFactory))(json).recover{case t => t.printStackTrace(); nullElement}
      }
    } yield result
  }

  def deleteSingle(e:Json):Future[Int] = {
    val id = e.ID(metadata.keys)
    jsonAction.delete(id)
  }


  def deleteChild(child:JSONMetadata, receivedJson:Seq[Json], dbJson:Seq[Json]): Seq[Future[Int]] = {
    val receivedID = receivedJson.map(_.ID(child.keys))
    val dbID = dbJson.map(_.ID(child.keys))
    logger.debug(s"child: ${child.name} received: ${receivedID.map(_.asString)} db: ${dbID.map(_.asString)}")
    dbID.filterNot(k => receivedID.contains(k)).map{ idsToDelete =>
      logger.info(s"Deleting child ${child.name}, with key: $idsToDelete")
      jsonActions(child.entity).delete(idsToDelete)
    }
  }

  def delete(id:JSONID) = {
    for{
      json <- getById(id)
      subs <- Future.sequence(subAction(json,0, _.deleteSingle))
      current <- deleteSingle(json)
    } yield current + subs.flatten.sum
  }

  def insert(e:Json):Future[Json] = for{
    inserted <- jsonAction.insert(e)
    _ <- Future.sequence(metadata.fields.filter(_.child.isDefined).map { field =>
      for {
        metadata <- metadataFactory.of(field.child.get.objId, metadata.lang)
        rows = attachArrayIndex(e.seq(field.name),metadata)
        //attach parent id
        rowsWithId = rows.map{ row =>
          val masterChild: Seq[(String, String)] = field.child.get.masterFields.split(",").zip(field.child.get.childFields.split(",")).toSeq
          masterChild.foldLeft(row){ case (acc,(master,child)) => acc.deepMerge(Json.obj(child -> inserted.js(master)))}
        }
        result <- FutureUtils.seqFutures(rowsWithId)(row => FormActions(metadata,jsonActions,metadataFactory).insert(row))
      } yield result
    })
    data <- getById(inserted.ID(metadata.keys))
  } yield data


  def update(e:Json):Future[Json] = {

    val id = e.ID(metadata.keys)
    for{
      _ <- Future.sequence(subAction(e,Json.Null,_.upsert))  //need upsert to add new child records
      result <- jsonAction.update(id,e)
    } yield result
  }

  def updateIfNeeded(e:Json):Future[Json] = {

    val id = e.ID(metadata.keys)
    for{
      _ <- Future.sequence(subAction(e,Json.Null,_.upsertIfNeeded))  //need upsert to add new child records
      result <- jsonAction.updateIfNeeded(id,e)
    } yield result
  }

  def upsert(e:Json):Future[Json] = {

    val id = e.ID(metadata.keys)
    for{
      _ <- Future.sequence(subAction(e,Json.Null,_.upsert))
      result <- jsonAction.upsert(id,e)
    } yield result
  }

  def upsertIfNeeded(e:Json):Future[Json] = {

    val id = e.ID(metadata.keys)
    for{
      _ <- Future.sequence(subAction(e,Json.Null,_.upsertIfNeeded))
      result <- jsonAction.upsertIfNeeded(id,e)
    } yield result
  }

  private def createQuery(entity:Json, child: Child):JSONQuery = {
    val parentFilter = for{
      (local,remote) <- child.masterFields.split(",").zip(child.childFields.split(","))
    } yield {
      JSONQueryFilter(remote,Some(Filter.EQUALS),entity.get(local))
    }

    val filters = parentFilter.toSeq ++ child.childQuery.toSeq.flatMap(_.filter)



    child.childQuery.getOrElse(JSONQuery.empty).copy(filter=filters.toList.distinct)
  }

  private def getChild(dataJson:Json, field:JSONField, metadata:JSONMetadata, child:Child):Future[Seq[Json]] = {
    val query = createQuery(dataJson,child)
    FormActions(metadata,jsonActions,metadataFactory).streamSeq(query).runFold(Seq[Json]())(_ ++ Seq(_))
  }

  private def expandJson(dataJson:Json):Future[Json] = {

    val values = metadata.fields.map{ field =>
      {(field.`type`,field.child) match {
        case ("static",_) => Future.successful(field.name -> field.default.asJson)  //set default value
        case (_,None) => Future.successful(field.name -> dataJson.js(field.name))        //use given value
        case (_,Some(child)) => for{
          form <- metadataFactory.of(child.objId,metadata.lang)
          data <- getChild(dataJson,field,form,child)
        } yield {
          logger.info(s"expanding child ${field.name} : ${data.asJson}")
          field.name -> data.asJson
        }
      }}.recover{ case t =>
        t.printStackTrace()
        field.name -> Json.Null
      }
    }
    Future.sequence(values).map(_.toMap.asJson)
  }



}
