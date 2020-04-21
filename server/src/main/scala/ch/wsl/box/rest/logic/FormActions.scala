package ch.wsl.box.rest.logic

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl.box.jdbc.PostgresProfile
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
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.MetadataFactory

import scala.concurrent.{ExecutionContext, Future}


case class ReferenceKey(localField:String,remoteField:String,value:String)
case class Reference(association:Seq[ReferenceKey])

case class FormActions(metadata:JSONMetadata,
                       jsonActions: String => TableActions[Json],
                       metadataFactory: MetadataFactory
                      )(implicit db:Database, mat:Materializer, ec:ExecutionContext) extends DBFiltersImpl with Logging with TableActions[Json] {

  import ch.wsl.box.shared.utils.JSONUtils._



  val jsonAction = jsonActions(metadata.entity)



  def getById(id:JSONID)(implicit db: Database):Future[Option[Json]] = {
    logger.info("Getting Form data")
    get(id.query)
  }

  private def streamSeq(query:JSONQuery):Source[Json,NotUsed] = {
    Source
      .fromPublisher(jsonAction.findStreamed(query))
      .flatMapConcat( json => Source.fromFuture(expandJson(json)))
  }
  def streamArray(query:JSONQuery):Source[Json,NotUsed] = streamSeq(query)
  def get(query:JSONQuery):Future[Option[Json]] = streamSeq(query).runFold(Seq[Json]())(_ ++ Seq(_)).map(x =>
      if(x.length >1){
        throw new Exception("Multiple rows retrieved with single id")
      } else {
        x.headOption.map(_.asJson)
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

  def subAction[T](e:Json, action: FormActions => ((JSONID,Json) => Future[_])): Seq[Future[List[_]]] = metadata.fields.filter(_.child.isDefined).map { field =>
    for {
      form <- metadataFactory.of(field.child.get.objId, metadata.lang)
      dbSubforms <- getChild(e,field,form,field.child.get)
      subJson = attachArrayIndex(e.seq(field.name),form)
      deleted = deleteChild(form,subJson,dbSubforms)
      result <- FutureUtils.seqFutures(subJson){ json => //order matters so we do it synchro
        action(FormActions(form,jsonActions,metadataFactory))(json.ID(form.keys),json).map(x => Some(x)).recover{case t => t.printStackTrace(); None}
      }.map(_.flatten)
    } yield result
  }

  def deleteSingle(id: JSONID,e:Json):Future[Int] = {
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


  def delete(id:JSONID)(implicit db: PostgresProfile.api.Database): Future[Int] = {
    for{
      json <- getById(id)
      subs <- Future.sequence(subAction(json.get,_.deleteSingle))
      current <- deleteSingle(id,json.get)
    } yield current + subs.size
  }

  def insert(e:Json)(implicit db: PostgresProfile.api.Database):Future[JSONID] = for{
    insertedId <- jsonAction.insert(e)
    inserted <- jsonAction.getById(insertedId).map(_.get)
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
  } yield insertedId



  def update(id:JSONID, e:Json)(implicit db: Database):Future[Int] = {
    for{
      _ <- Future.sequence(subAction(e,_.upsertIfNeeded))  //need upsert to add new child records
      result <- jsonAction.update(id,e)
    } yield result
  }

  def updateIfNeeded(id:JSONID, e:Json)(implicit db: Database):Future[Int] = {

    for{
      _ <- Future.sequence(subAction(e,_.upsertIfNeeded))  //need upsert to add new child records
      result <- jsonAction.updateIfNeeded(id,e)
    } yield result
  }

  def upsertIfNeeded(id:JSONID,e:Json)(implicit db:Database):Future[JSONID] = {

    for{
      newId <- jsonAction.upsertIfNeeded(id,e)
      model <- jsonAction.getById(newId) // in some circumstances returns null, probably with autoincrements fields
      _ <- Future.sequence(subAction(model.get,_.upsertIfNeeded))
    } yield newId
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






  override def findStreamed(query: JSONQuery)(implicit db: PostgresProfile.api.Database): DatabasePublisher[Json] = jsonAction.findStreamed(query)


  override def count()(implicit db: PostgresProfile.api.Database): Future[JSONCount] = jsonAction.count()

  override def ids(query: JSONQuery)(implicit db: PostgresProfile.api.Database, mat: Materializer): Future[IDs] = jsonAction.ids(query)
}
