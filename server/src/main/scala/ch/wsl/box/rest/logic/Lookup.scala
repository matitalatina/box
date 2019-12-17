package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.EntityActionsRegistry
import ch.wsl.box.model.shared.{JSONFieldLookup, JSONLookup, JSONMetadata, JSONQuery}
import io.circe.Json
import ch.wsl.box.rest.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object Lookup {



  import ch.wsl.box.shared.utils.JSONUtils._

  def valuesForEntity(metadata:JSONMetadata)(implicit ec: ExecutionContext, db:Database,  mat:Materializer) :Future[Map[String,Seq[Json]]] = {
      val actionsRegistry = EntityActionsRegistry()

      Future.sequence{
        metadata.fields.flatMap(_.lookup.map(_.lookupEntity)).map{ lookupEntity =>
          actionsRegistry.tableActions(lookupEntity).find(JSONQuery.empty).map{ jq => lookupEntity -> jq}
        }
      }.map(_.toMap)
  }

  def valueExtractor(lookupElements:Option[Map[String,Seq[Json]]],metadata:JSONMetadata)(field:String, value:String):String = {

    for{
      elements <- lookupElements
      field <- metadata.fields.find(_.name == field)
      lookup <- field.lookup
      foreignEntity <- elements.get(lookup.lookupEntity)
      foreignRow <- foreignEntity.find(_.get(lookup.map.valueProperty) == value)
    } yield foreignRow.get(lookup.map.textProperty)

  }.getOrElse(value)

  def values(entity:String,value:String,text:String,query:JSONQuery)(implicit ec: ExecutionContext, db:Database,  mat:Materializer) :Future[Seq[JSONLookup]] = {
    val actionsRegistry = EntityActionsRegistry()
    actionsRegistry.actions(entity).get.find(query).map{ _.map{ row =>
      val label = text.split(",").map(x => row.get(x.trim)).mkString(" - ")
      JSONLookup(row.get(value),label)
    }}
  }
}
