package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.shared.{JSONFieldLookup, JSONLookup, JSONMetadata, JSONQuery}
import io.circe.Json
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.runtime.Registry

import scala.concurrent.{ExecutionContext, Future}

object Lookup {



  import ch.wsl.box.shared.utils.JSONUtils._

  def valuesForEntity(metadata:JSONMetadata)(implicit ec: ExecutionContext, db:Database,  mat:Materializer) :Future[Map[String,Seq[Json]]] = {
      val io = DBIO.sequence{
        metadata.fields.flatMap(_.lookup.map(_.lookupEntity)).map{ lookupEntity =>
          Registry().actions.tableActions(ec)(lookupEntity).find(JSONQuery.empty).map{ jq => lookupEntity -> jq}
        }
      }.map(_.toMap)
      db.run(io)
  }

  def valueExtractor(lookupElements:Option[Map[String,Seq[Json]]],metadata:JSONMetadata)(field:String, value:String):Option[String] = {

    for{
      elements <- lookupElements
      field <- metadata.fields.find(_.name == field)
      lookup <- field.lookup
      foreignEntity <- elements.get(lookup.lookupEntity)
      foreignRow <- foreignEntity.find(_.get(lookup.map.valueProperty) == value)
    } yield foreignRow.get(lookup.map.textProperty)

  }

  def values(entity:String,value:String,text:String,query:JSONQuery)(implicit ec: ExecutionContext, db:Database,  mat:Materializer) :Future[Seq[JSONLookup]] = {
    val io = Registry().actions.actions(entity)(ec).get.find(query).map{ _.map{ row =>
      val label = text.split(",").map(x => row.get(x.trim)).mkString(" - ")
      JSONLookup(row.get(value),label)
    }}
    db.run(io)
  }
}
