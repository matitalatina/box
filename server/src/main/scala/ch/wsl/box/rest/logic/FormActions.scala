package ch.wsl.box.rest.logic

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import ch.wsl
import ch.wsl.box
import ch.wsl.box.jdbc
import ch.wsl.box.jdbc.{Connection, FullDatabase, PostgresProfile}
import io.circe._
import io.circe.syntax._
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.routes.enablers.CSVDownload
import ch.wsl.box.rest.utils.{FutureUtils, Timer, UserProfile}
import io.circe.Json
import scribe.Logging
import slick.basic.DatabasePublisher
import slick.lifted.Query
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.metadata.MetadataFactory
import ch.wsl.box.rest.runtime.Registry

import scala.concurrent.{ExecutionContext, Future}


case class ReferenceKey(localField:String,remoteField:String,value:String)
case class Reference(association:Seq[ReferenceKey])

case class FormActions(metadata:JSONMetadata,
                       jsonActions: String => TableActions[Json],
                       metadataFactory: MetadataFactory
                      )(implicit db:FullDatabase, mat:Materializer, ec:ExecutionContext) extends DBFiltersImpl with Logging with TableActions[Json] {

  import ch.wsl.box.shared.utils.JSONUtils._



  val jsonAction = jsonActions(metadata.entity)



  def getById(id:JSONID) = {
    logger.info("Getting Form data")


    jsonAction.getById(id).flatMap{ row =>
      DBIO.sequenceOption(row.map(expandJson))
    }
  }

  private def streamSeq(query:JSONQuery):DBIO[Seq[Json]] = {

    val q:JSONQuery = metadata.query.map{ defaultQuery =>
        JSONQuery(
          filter = defaultQuery.filter ++ query.filter,
          sort = defaultQuery.sort ++ query.sort,
          paging = defaultQuery.paging.orElse(query.paging),
          lang = defaultQuery.lang
        )
    }.getOrElse(query)

    jsonAction.find(q).flatMap{ rows =>
      DBIO.sequence(rows.map(expandJson))
    }

  }


  private def _list(query:JSONQuery):DBIO[Seq[Json]] = {
    metadata.view.map(v => Registry().actions(v)) match {
      case None => streamSeq(query)
      case Some(v) => v.find(query)
    }
  }

  def list(query:JSONQuery,lookupElements:Option[Map[String,Seq[Json]]]):DBIO[Seq[Json]] = _list(query).map{ _.map{ row =>


    val lookup = Lookup.valueExtractor(lookupElements, metadata) _

    val columns = metadata.tabularFields.map{f =>
      (f, lookup(f,row.js(f).string).map(_.asJson).getOrElse(row.js(f)))
    }
    Json.obj(columns:_*)
  }}

  def csv(query:JSONQuery,lookupElements:Option[Map[String,Seq[Json]]],fields:JSONMetadata => Seq[String] = _.tabularFields):DBIO[String] = {

    import kantan.csv._
    import kantan.csv.ops._

    val lookup = Lookup.valueExtractor(lookupElements, metadata) _

    _list(query).map { rows =>
      rows.map { json =>
        fields(metadata).map { field =>
          lookup(field, json.get(field)).getOrElse(json.get(field))
        }
      }.asCsv(rfc)
    }


  }





  /**
   * When default `arrayIndex` is specified the value of the field is substituted with
   * the index of the list. This is useful for keep the sorting of subforms.
   * If no arrayIndex is present is NOOP
   * @param jsonToInsert subforms JSON list
   * @param form subform metadata
   * @return subform JSON list with the sobstitution arrayIndex -> i
   */
  def attachArrayIndex(jsonToInsert:Seq[Json],form:JSONMetadata):Seq[Json] = {
    jsonToInsert.zipWithIndex.map{ case (jsonRow,i) =>
      val values = form.fields.filter(_.default.contains("arrayIndex")).map{ fieldToAdd =>
        fieldToAdd.name -> i
      }.toMap
      jsonRow.deepMerge(values.asJson) //overwrite field value with array index
    }
  }

  def attachParentId(jsonToInsert:Seq[Json],parentJson:Json, child:Child):Seq[Json] = {
    val values = child.masterFields.split(",").map(_.trim).zip(child.childFields.split(",").map(_.trim)).map{ case (parentKey,childKey) =>
      childKey -> parentJson.js(parentKey)
    }.toMap

    jsonToInsert.map{ jsonRow =>
      jsonRow.deepMerge(values.asJson) //overwrite field value with array index
    }
  }

  def subAction[T](e:Json, action: FormActions => ((Option[JSONID],Json) => DBIO[_])): Seq[DBIO[Seq[_]]] = metadata.fields.filter(_.child.isDefined).map { field =>
    for {
      form <- DBIO.from(Connection.adminDB.run(metadataFactory.of(field.child.get.objId, metadata.lang)))
      dbSubforms <- getChild(e,form,field.child.get)
      subs = e.seq(field.name)
      subJsonWithIndexs = attachArrayIndex(subs,form)
      subJson = attachParentId(subJsonWithIndexs,e,field.child.get)
      deleted <- DBIO.sequence(deleteChild(form,subJson,dbSubforms))
      result <- DBIO.sequence(subJson.map{ json => //order matters so we do it synchro
          action(FormActions(form,jsonActions,metadataFactory))(json.ID(form.keys),json).map(x => Some(x))
      }).map(_.flatten)
    } yield result
  }

  def deleteSingle(id: JSONID,e:Json) = {
    jsonAction.delete(id)
  }


  /**
   * Delete child if removed from parent JSON
   * @param child child form metadata
   * @param receivedJson list of child recived
   * @param dbJson list of child present in DB
   * @return number of deleted rows
   */
  def deleteChild(child:JSONMetadata, receivedJson:Seq[Json], dbJson:Seq[Json]): Seq[DBIO[Int]] = {
    val receivedID = receivedJson.flatMap(_.ID(child.keys))
    val dbID = dbJson.flatMap(_.ID(child.keys))
    logger.debug(s"child: ${child.name} received: ${receivedID.map(_.asString)} db: ${dbID.map(_.asString)}")
    dbID.filterNot(k => receivedID.contains(k)).map{ idsToDelete =>
      logger.info(s"Deleting child ${child.name}, with key: $idsToDelete")
      jsonActions(child.entity).delete(idsToDelete)
    }
  }


  def delete(id:JSONID) = {
    for{
      json <- getById(id)
      subs <- DBIO.sequence(subAction(json.get,x => (id,json) => x.deleteSingle(id.get,json)))
      current <- deleteSingle(id,json.get)
    } yield current + subs.size
  }

  def insert(e:Json) = for{
    insertedId <- jsonAction.insert(e)
    inserted <- jsonAction.getById(insertedId).map(_.get)
    _ <- DBIO.sequence(metadata.fields.filter(_.child.isDefined).map { field =>
      for {
        metadata <- DBIO.from(Connection.adminDB.run(metadataFactory.of(field.child.get.objId, metadata.lang)))
        rows = attachArrayIndex(e.seq(field.name),metadata)
        //attach parent id
        rowsWithId = rows.map{ row =>
          val masterChild: Seq[(String, String)] = field.child.get.masterFields.split(",").map(_.trim).zip(field.child.get.childFields.split(",").map(_.trim)).toSeq
          masterChild.foldLeft(row){ case (acc,(master,child)) => acc.deepMerge(Json.obj(child -> inserted.js(master)))}
        }
        result <- DBIO.sequence(rowsWithId.map(row => FormActions(metadata,jsonActions,metadataFactory).insert(row)))
      } yield result
    })
  } yield insertedId



  def update(id:JSONID, e:Json) = {
    for{
      _ <- DBIO.sequence(subAction(e,_.upsertIfNeeded))  //need upsert to add new child records
      result <- jsonAction.update(id,e)
    } yield result
  }

  def updateIfNeeded(id:JSONID, e:Json) = {

    for{
      _ <- DBIO.sequence(subAction(e,_.upsertIfNeeded))  //need upsert to add new child records
      result <- jsonAction.updateIfNeeded(id,e)
    } yield result
  }

  def upsertIfNeeded(id:Option[JSONID],e:Json) = {

    for{
      newId <- jsonAction.upsertIfNeeded(id,e)
      model <- jsonAction.getById(newId)
      newModel = e.deepMerge(model.get) // takes the autogenerated values from db and attach the childs that are in the JSON
      _ <- DBIO.sequence(subAction(newModel,_.upsertIfNeeded))
    } yield newId
  }

  private def createQuery(entity:Json, child: Child):JSONQuery = {
    val parentFilter = for{
      (local,remote) <- child.masterFields.split(",").map(_.trim).zip(child.childFields.split(",").map(_.trim))
    } yield {
      JSONQueryFilter(remote,Some(Filter.EQUALS),entity.get(local))
    }

    val filters = parentFilter.toSeq ++ child.childQuery.toSeq.flatMap(_.filter)



    child.childQuery.getOrElse(JSONQuery.empty).copy(filter=filters.toList.distinct)
  }

  private def getChild(dataJson:Json, metadata:JSONMetadata, child:Child):DBIO[Seq[Json]] = {
    val query = createQuery(dataJson,child)
    FormActions(metadata,jsonActions,metadataFactory).streamSeq(query)
  }

  private def expandJson(dataJson:Json):DBIO[Json] = {

    val values = metadata.fields.map{ field =>
      {(field.`type`,field.child) match {
        case ("static",_) => DBIO.successful(field.name -> field.default.asJson)  //set default value
        case (_,None) => DBIO.successful(field.name -> dataJson.js(field.name))        //use given value
        case (_,Some(child)) => for{
          form <- DBIO.from(Connection.adminDB.run(metadataFactory.of(child.objId,metadata.lang)))
          data <- getChild(dataJson,form,child)
        } yield {
          logger.info(s"expanding child ${field.name} : ${data.asJson}")
          field.name -> data.asJson
        }
      }}
    }
    DBIO.sequence(values).map(_.toMap.asJson)
  }


  override def find(query: JSONQuery) = jsonAction.find(query)

  override def count() = jsonAction.count()
  override def count(query: JSONQuery) = jsonAction.count(query)

  override def ids(query: JSONQuery) = {
    metadata.view.map(v => Registry().actions(v)) match {
      case None => jsonAction.ids(query)
      case Some(v) => for {
        data <- v.find(query)
        n <- v.count(query)
      } yield {
        val last = query.paging match {
          case None => true
          case Some(paging) => (paging.currentPage * paging.pageLength) >= n
        }
        IDs(
          last,
          query.paging.map(_.currentPage).getOrElse(1),
          data.flatMap { x => JSONID.fromData(x, metadata).map(_.asString) },
          n
        )
      }
    }
  }
}
